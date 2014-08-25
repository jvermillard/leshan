var lwClientControllers = angular.module('clientControllers', []);

lwClientControllers.controller('ClientListCtrl', [
    '$scope',
    '$http',
    '$location',
    function ClientListCtrl($scope, $http,$location) {

        // update navbar
        angular.element("#navbar").children().removeClass('active');
        angular.element("#client-navlink").addClass('active');

        // free resource when controller is destroyed
        $scope.$on('$destroy', function(){
            if ($scope.eventsource){
                $scope.eventsource.close()
            }
        });

        // add function to show client
        $scope.showClient = function(client) {
            $location.path('/clients/' + client.endpoint);
        };
        
        // get the list of connected clients
        $http.get('api/clients'). error(function(data, status, headers, config){
            $scope.error = "Unable get client list: " + status + " " + data  
            console.error($scope.error)
        }).success(function(data, status, headers, config) {
            $scope.clients = data;

            // HACK : we can not use ng-if="clients"
            // because of https://github.com/angular/angular.js/issues/3969
            $scope.clientslist = true;

            // listen for clients registration/deregistration
            $scope.eventsource = new EventSource('event');

            var registerCallback = function(msg) {
                $scope.$apply(function() {
                    var client = JSON.parse(msg.data);
                    $scope.clients.push(client);
                });
            }
            $scope.eventsource.addEventListener('REGISTRATION', registerCallback, false);

            var getClientIdx = function(client) {
                for (var i = 0; i < $scope.clients.length; i++) {
                    if ($scope.clients[i].registrationId == client.registrationId) {
                        return i;
                    }
                }
                return -1;
            }
            var deregisterCallback = function(msg) {
                $scope.$apply(function() {
                    var clientIdx = getClientIdx(JSON.parse(msg.data));
                    if(clientIdx >= 0) {
                        $scope.clients.splice(clientIdx, 1);
                    }
                });
            }
            $scope.eventsource.addEventListener('DEREGISTRATION', deregisterCallback, false);
        });
}]);

lwClientControllers.controller('ClientDetailCtrl', [
    '$scope',
    '$location',
    '$routeParams',
    '$http',
    'lwResources',
    '$filter',
    function($scope, $location, $routeParams, $http, lwResources,$filter) {
        // update navbar
        angular.element("#navbar").children().removeClass('active');
        angular.element("#client-navlink").addClass('active');
        
        // free resource when controller is destroyed
        $scope.$on('$destroy', function(){
            if ($scope.eventsource){
                $scope.eventsource.close()
            }
        });

        $scope.clientId = $routeParams.clientId;

        // get client details
        $http.get('api/clients/' + $routeParams.clientId)
        .error(function(data, status, headers, config) {
            $scope.error = "Unable get client " + $routeParams.clientId+" : "+ status + " " + data;  
            console.error($scope.error);
        })
        .success(function(data, status, headers, config) {
            $scope.client = data;

            // update resource tree with client details
            $scope.objects = lwResources.buildResourceTree($scope.client.objectLinks);

            // listen for clients registration/deregistration/observe
            $scope.eventsource = new EventSource('event?ep=' + $routeParams.clientId);

            var registerCallback = function(msg) {
                $scope.$apply(function() {
                    $scope.deregistered = false;
                    $scope.client = JSON.parse(msg.data);
                    $scope.objects = lwResources.buildResourceTree($scope.client.objectLinks);
                });
            }
            $scope.eventsource.addEventListener('REGISTRATION', registerCallback, false);

            var deregisterCallback = function(msg) {
                $scope.$apply(function() {
                    $scope.deregistered = true;
                    $scope.client = null;
                });
            }
            $scope.eventsource.addEventListener('DEREGISTRATION', deregisterCallback, false);

            var notificationCallback = function(msg) {
                $scope.$apply(function() {
                    var content = JSON.parse(msg.data);
                    var resource = lwResources.findResource($scope.objects, content.res);
                    if (resource) {
                        resource.value = content.val;
                        resource.valuesupposed = false;
                        resource.observed = true;

                        var formattedDate = $filter('date')(new Date(), 'HH:mm:ss.sss');
                        resource.tooltip = formattedDate;
                    }
                });
            }
            $scope.eventsource.addEventListener('NOTIFICATION', notificationCallback, false);
        });
}]);
