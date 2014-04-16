var lwClientControllers = angular.module('lwClientControllers', []);

lwClientControllers.controller('ClientListCtrl', [
    '$scope',
    '$http',
    function ClientListCtrl($scope, $http) {

        // get the list of connected clients
        $http.get('/api/clients').success(
            function(data, status, headers, config) {
            $scope.clients = data;
        }). error(function(data, status, headers, config) {
            console.error("Unable get client list:", status, data)
        });

        // listen for clients registration/deregistration
        var source = new EventSource('/event');

        var registerCallback = function(msg) {
            $scope.$apply(function() {
                var client = JSON.parse(msg.data);
                $scope.clients.push(client);
            });
        }
        source.addEventListener('REGISTRATION', registerCallback, false);

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
        source.addEventListener('DEREGISTRATION', deregisterCallback, false);

    } ]);

    lwClientControllers.controller('ClientDetailCtrl', [
        '$scope',
        '$location',
        '$routeParams',
        '$http',
        function($scope, $location, $routeParams, $http) {
            $scope.clientId = $routeParams.clientId;

            // get client details
            $http.get('/api/clients/' + $routeParams.clientId)
            .success(function(data, status, headers, config) {
                $scope.client = data;

                // get the lw resources description
                $http.get('/json/lw-resources.json').success(
                    function(data, status, headers, config) {
                    // update resource tree with client details
                    var tree = buildResourceTree($scope.client.objectLinks, data)
                    $scope.lwresources = tree;
                }).error(function(data, status, headers, config) {
                    console.error("Unable to load lw-resources.json :", status, data)
                });;

            }). error(function(data, status, headers, config) {
                console.error("Unable get client",$routeParams.clientId, ":", status, data)
            });;

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
                        treeNode.push(lwNode);
                        if(nodeIds.length > 0) {
                            // clean up children nodes and add next node
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

        } ]);
