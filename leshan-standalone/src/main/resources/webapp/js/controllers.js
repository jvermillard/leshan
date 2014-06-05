var lwClientControllers = angular.module('lwClientControllers', []);

lwClientControllers.controller('ClientListCtrl', [
    '$scope',
    '$http',
    function ClientListCtrl($scope, $http) {
        // free resource when controller is destroyed
        $scope.$on('$destroy', function(){
            if ($scope.eventsource){
                $scope.eventsource.close()
            }
        });

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
            var tree = buildResourceTree($scope.client.objectLinks, lwResources.getModel())
            $scope.lwresources = tree;

            // listen for clients registration/deregistration
            $scope.eventsource = new EventSource('event?ep=' + $routeParams.clientId);

            var registerCallback = function(msg) {
                $scope.$apply(function() {
                    $scope.deregistered = false;
                    $scope.client = JSON.parse(msg.data);
                    var tree = buildResourceTree($scope.client.objectLinks, lwResources.getModel())
                    $scope.lwresources = tree;
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
                    var resourceId = content.res.split("/");
                    var resource = findResource(resourceId, $scope.lwresources);
                    if (resource) {
                        resource.observe.status = true;
                    	resource.read.value = content.val;
                    	resource.read.status = "CONTENT";
                        resource.read.date = new Date();
                        var formattedDate = $filter('date')(resource.read.date, 'HH:mm:ss.sss');
                        resource.read.tooltip = formattedDate + " " + resource.read.status;
                    	resource.write.value = null;
                    }
                });
            }
            $scope.eventsource.addEventListener('NOTIFICATION', notificationCallback, false);
        });

        var buildResourceTree = function(objectLinks, lwResources) {
            var tree = [];
            for (var i = 0; i < objectLinks.length; i++) {
                var nodeIds = objectLinks[i].replace("</", "").replace(">", "").split("/");
                addNodes(tree, lwResources, nodeIds);
            }
            return tree;
        }

        var addNodes = function(treeNode, lwNodes, nodeIds) {
            var nodeId  = nodeIds.shift();

            // node in lw resources ?
            var lwNode = findNode(nodeId, lwNodes);

            // already in tree ?
            var existing = findNode(nodeId, treeNode);
            if(existing) {
                if(nodeIds.length > 0) {
                    var lwChildNodes;
                    if(lwNode) {
                        lwChildNodes = lwNode.values;
                    }
                    addNodes(existing.values, lwChildNodes, nodeIds);
                }
            }
            else {
                if(lwNode) {
                    // add properties for tracking observations
                    lwNode.observationId = null;
                    lwNode.observed = false;
                    treeNode.push(lwNode);
                    if(nodeIds.length > 0) {
                        // this is not a resource, thus
                        // remove children defined by lw-resources.json
                        // and add nodes at next level
                        var newNode = treeNode[treeNode.length - 1];
                        newNode.values = [];
                        addNodes(newNode.values, lwNode.values, nodeIds);
                    }
                }
                else {
                    // add new custom node
                    var customNode = {};
                    customNode.name = nodeId;
                    customNode.id = nodeId;
                    customNode.operations = "RW";

                    if(nodeIds.length > 0) {
                        customNode.values = [];
                        addNodes(customNode.values, null, nodeIds);
                    }

                    treeNode.push(customNode);
                }
            }
        }

        var findNode = function(id, nodes) {
            if(nodes) {
                for (var i = 0; i < nodes.length; i++) {
                    if(nodes[i].id == id) {
                        return nodes[i];
                    }
                }
            }
        }
        
        var findResource = function(resourceId, resourceTree) {
            if (resourceId) {
                var nodeId = resourceId.shift();
                var node = findNode(nodeId, resourceTree);
                if (node && resourceId.length > 0) {
                    return findResource(resourceId, node.values);
                } else {
                    return node;
                }
            	}
        }
}]);
