angular.module('lwResourcesDirective', [])

.directive('resourceslist', function () {
    return {
        restrict: "E",
        replace: true,
        scope: {
            list: '=',
            parent: '='
        },
        template: "<ul'><resource ng-repeat='resource in list' resource='resource' parent='parent'></resource></ul>"
    }
})

.directive('resource', function ($compile, $routeParams, $http) {
    return {
        restrict: "E",
        replace: true,
        scope: {
            resource: '=',
            parent: '='
        },
        templateUrl: "partials/resource.html",
        link: function (scope, element, attrs) {
            // compute path and tree depth
            var parentPath = "";
            var treeDepth = 0;
            if(scope.parent) {
                parentPath = scope.parent.path;
                treeDepth = scope.parent.treeDepth + 1;
            }
            scope.resource.path = parentPath + "/" + scope.resource.id;
            scope.resource.treeDepth = treeDepth;
            scope.resource.read  =  {tooltip : "Read "   + scope.resource.path};
            scope.resource.write =  {tooltip : "Write "  + scope.resource.path};
            scope.resource.exec  =  {tooltip : "Execute "+ scope.resource.path};
            
            scope.readable = function() {
                if(scope.resource.instances != "multiple") {
                    if(scope.resource.hasOwnProperty("operations")) {
                        return scope.resource.operations.indexOf("R") != -1;
                    }
                }
                return false;
            }

            scope.writable = function() {
                if(scope.resource.instances != "multiple") {
                    if(scope.resource.hasOwnProperty("operations")) {
                        return scope.resource.operations.indexOf("W") != -1;
                    }
                }
                return false;
            }

            scope.executable = function() {
                if(scope.resource.instances != "multiple") {
                    if(scope.resource.hasOwnProperty("operations")) {
                        return scope.resource.operations.indexOf("E") != -1;
                    }
                }
                return false;
            }

            scope.read = function() {
                $http.get("api/clients/" + $routeParams.clientId + scope.resource.path)
                .success(function(data, status, headers, config) {
                    var read = scope.resource.read;
                    if (data.status == "CONTENT") {
                        read.value = data.value;
                        scope.resource.write.value = null
                    }
                    read.status = data.status;
                    read.date = new Date();
                    var formattedDate = read.date.getHours()+":"+read.date.getMinutes()+":"+read.date.getSeconds()+":"+read.date.getMilliseconds();
                    read.tooltip = formattedDate + " " + read.status;
                }).error(function(data, status, headers, config) {
                    errormessage = "Unable to read resource " + scope.resource.path + " for "+ $routeParams.clientId + " : " + status +" "+ data
                    alert(errormessage);
                    console.error(errormessage)
                });;
            };

            scope.write = function() {
                $('#writeModalLabel').text(scope.resource.name);
                $('#writeInputValue').val(scope.resource.value);
                $('#writeSubmit').unbind();
                $('#writeSubmit').click(function(e){
                    e.preventDefault();
                    var value = $('#writeInputValue').val();
                    if(value) {
                        $('#writeModal').modal('hide');
                        $http({method: 'PUT', url: "api/clients/" + $routeParams.clientId + scope.resource.path, data: value, headers:{'Content-Type': 'text/plain'}})
                        .success(function(data, status, headers, config) {
                            write = scope.resource.write;
                            if (data.status == "CHANGED") {
                                scope.resource.read.value = null;
                                write.value = value;
                            }
                            write.status = data.status;
                            write.date = new Date();
                            var formattedDate = write.date.getHours()+":"+write.date.getMinutes()+":"+write.date.getSeconds()+":"+write.date.getMilliseconds();
                            write.tooltip = formattedDate + " " + write.status;
                        }).error(function(data, status, headers, config) {
                            errormessage = "Unable to write resource " + scope.resource.path + " for "+ $routeParams.clientId + " : " + status +" "+ data
                            alert(errormessage);
                            console.error(errormessage)
                        });;
                    }
                });

                $('#writeModal').modal('show');
            };

            scope.exec = function() {
                $http.post("api/clients/" + $routeParams.clientId+ scope.resource.path)
                .success(function(data, status, headers, config) {
                    var exec = scope.resource.exec;
                    exec.status = data.status;
                    exec.date = new Date();
                    var formattedDate = exec.date.getHours()+":"+exec.date.getMinutes()+":"+exec.date.getSeconds()+":"+exec.date.getMilliseconds();
                    exec.tooltip = formattedDate + " " + exec.status;
                }).error(function(data, status, headers, config) {
                    errormessage = "Unable to execute resource " + scope.resource.path + " for "+ $routeParams.clientId + " : " + status +" "+ data
                    alert(errormessage);
                    console.error(errormessage)
                });;
            };

            // add children
            var collectionSt = '<resourceslist list="resource.values" parent="resource"></resourceslist>';
            if (angular.isArray(scope.resource.values)) {
                $compile(collectionSt)(scope, function(cloned, scope)   {
                    element.append(cloned);
                });
            }
        }
    }
});
