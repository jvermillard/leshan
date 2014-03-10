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
                $http.get("/api/clients/" + $routeParams.clientId + scope.resource.path)
                .success(function(data, status, headers, config) {
                    // alert(JSON.stringify(data));
                    if (data.status = "CONTENT") {
                        scope.resource.value = data.value;
                    }
                });
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
                        $http({method: 'PUT', url: "/api/clients/" + $routeParams.clientId + scope.resource.path, data: value, headers:{'Content-Type': 'text/plain'}})
                        .success(function(data, status, headers, config) {
                            // alert(JSON.stringify(data));
                            if (data.status = "CONTENT") {
                                scope.resource.value = value;
                            }
                        });
                    }
                });

                $('#writeModal').modal('show');
            };

            scope.exec = function() {
                $http.post("/api/clients/" + $routeParams.clientId + scope.resource.path)
                .success(function(data, status, headers, config) {
                    alert("success!");
                });
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
