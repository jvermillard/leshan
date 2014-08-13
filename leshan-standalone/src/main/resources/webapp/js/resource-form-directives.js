angular.module('resourceFormDirectives', [])

.directive('resourceform', function ($compile, $routeParams, $http, dialog,$filter) {
    return {
        restrict: "E",
        replace: true,
        scope: {
            resource: '=',
            parent: '='
        },
        templateUrl: "partials/resource-form.html",
        link: function (scope, element, attrs) {
            scope.writable = function() {
                if(scope.resource.def.instances != "multiple") {
                    if(scope.resource.def.hasOwnProperty("operations")) {
                        return scope.resource.def.operations.indexOf("W") != -1;
                    }
                }
                return false;
            }
        }
    }
});