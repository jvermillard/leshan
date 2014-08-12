angular.module('instanceDirectives', [])

.directive('instance', function ($compile, $routeParams, $http, dialog,$filter, lwResources) {
    return {
        restrict: "E",
        replace: true,
        scope: {
            instance: '=',
            parent: '='
        },
        templateUrl: "partials/instance.html",
        link: function (scope, element, attrs) {
            var parentPath = "";
            scope.instance.path = scope.parent.path + "/" + scope.instance.id;
            
            scope.instance.read  =  {tooltip : "Read <br/>"   + scope.instance.path};
            scope.instance.del  =  {tooltip : "Delete <br/>"   + scope.instance.path};
            scope.instance.write =  {tooltip : "Write <br/>"  + scope.instance.path};
           
            scope.read = function() {
                var uri = "api/clients/" + $routeParams.clientId + scope.instance.path;                
                $http.get(uri)
                .success(function(data, status, headers, config) {
                    // manage request information
                    var read = scope.instance.read;
                    read.date = new Date();
                    var formattedDate = $filter('date')(read.date, 'HH:mm:ss.sss');
                    read.status = data.status;
                    read.tooltip = formattedDate + "<br/>" + read.status;
                    
                    // manage read data
                    if (data.status == "CONTENT") {
                        for (var i in data.value) {
                            var tlvresource = data.value[i];
                            resource = lwResources.addResource(scope.parent, scope.instance, tlvresource.id, null)
                            resource.value = tlvresource.value;
                            resource.valuesupposed = false;
                            resource.tooltip = formattedDate;
                        }
                    }
                }).error(function(data, status, headers, config) {
                    errormessage = "Unable to read instance " + scope.instance.path + " for "+ $routeParams.clientId + " : " + status +" "+ data
                    dialog.open(errormessage);
                    console.error(errormessage)
                });;
            };
            
            
            scope.del = function() {
                var uri = "api/clients/" + $routeParams.clientId + scope.instance.path;                
                $http.delete(uri)
                .success(function(data, status, headers, config) {
                    // manage request information
                    var del = scope.instance.del;
                    del.date = new Date();
                    var formattedDate = $filter('date')(del.date, 'HH:mm:ss.sss');
                    del.status = data.status;
                    del.tooltip = formattedDate + "<br/>" + del.status;
                    
                    // manage read data
                    if (data.status == "DELETED") {
                        scope.parent.instances.splice(scope.instance,1);
                    }
                }).error(function(data, status, headers, config) {
                    errormessage = "Unable to delete instance " + scope.instance.path + " for "+ $routeParams.clientId + " : " + status +" "+ data
                    dialog.open(errormessage);
                    console.error(errormessage)
                });;
            };
        }
    }
});
