angular.module('objectDirectives', [])

.directive('object', function ($compile, $routeParams, $http, dialog,$filter) {
    return {
        restrict: "E",
        replace: true,
        scope: {
            object: '=',
            parent: '='
        },
        templateUrl: "partials/object.html",
        link: function (scope, element, attrs) {
            var parentPath = "";
            if(scope.parent) {
                parentPath = scope.parent.path;
            }
            scope.status = {};
            scope.status.open = true;
            
            scope.object.path = parentPath + "/" + scope.object.id;
        }
    }
});
