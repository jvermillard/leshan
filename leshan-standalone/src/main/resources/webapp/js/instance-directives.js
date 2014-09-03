/*!
 * Copyright (c) 2013-2014, Sierra Wireless
 * Released under the BSD license
 */

angular.module('instanceDirectives', [])

.directive('instance', function ($compile, $routeParams, $http, dialog,$filter, lwResources, $modal) {
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
            scope.instance.write =  {tooltip : "Write <br/>"  + scope.instance.path};
            scope.instance.del  =  {tooltip : "Delete <br/>"   + scope.instance.path};
            
           
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
                            if (tlvresource.type == "MULTIPLE_RESOURCE"){
                                var tab= new Array();
                                for (var i in tlvresource.resources){
                                    tab.push(tlvresource.resources[i].value)
                                }
                                resource.value = tab.join(", ");
                            }else{
                                resource.value = tlvresource.value;
                            }
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
                    
                    // manage delete instance in resource tree.
                    if (data.status == "DELETED") {
                        scope.parent.instances.splice(scope.instance,1);
                    }
                }).error(function(data, status, headers, config) {
                    errormessage = "Unable to delete instance " + scope.instance.path + " for "+ $routeParams.clientId + " : " + status +" "+ data
                    dialog.open(errormessage);
                    console.error(errormessage)
                });;
            };
            
            scope.write = function () {
                var modalInstance = $modal.open({
                  templateUrl: 'partials/modal-instance.html',
                  controller: 'modalInstanceController',
                  resolve: {
                    object: function(){ return scope.parent},
                    instanceId:function(){return scope.instance.id}
                  }
                });
            
                modalInstance.result.then(function (instance) {
                    // Build payload
                    var payload = []
                    for(i in instance.resources){
                        var resource = instance.resources[i];
                        if (resource.value != undefined){
                            payload.push({id:resource.id,type:'RESOURCE_VALUE',value:resource.value})
                        } 
                    }
                    // Send request
                    $http({method: 'PUT', url: "api/clients/" + $routeParams.clientId + scope.instance.path, data: payload, headers:{'Content-Type': 'application/json'}})
                    .success(function(data, status, headers, config) {
                        write = scope.instance.write;
                        write.date = new Date();
                        var formattedDate = $filter('date')(write.date, 'HH:mm:ss.sss');
                        write.status = data.status;
                        write.tooltip = formattedDate + "<br/>" + write.status;
                        
                        if (data.status == "CHANGED") {
                            for (var i in payload) {
                                var tlvresource = payload[i];
                                resource = lwResources.addResource(scope.parent, scope.instance, tlvresource.id, null)
                                resource.value = tlvresource.value;
                                resource.valuesupposed = true;
                                resource.tooltip = formattedDate;
                            }
                        }
                    }).error(function(data, status, headers, config) {
                        errormessage = "Unable to write resource " + scope.instance.path + " for "+ $routeParams.clientId + " : " + status +" "+ data
                        dialog.open(errormessage);
                        console.error(errormessage)
                    });;
                  console.log(instance);
                });
            };
        }
    }
});
