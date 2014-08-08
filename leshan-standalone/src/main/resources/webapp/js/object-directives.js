angular.module('objectDirectives', [])

//.directive('objectslist', function () {
//    return {
//        restrict: "E",
//        replace: true,
//        scope: {
//            list: '=',
//            parent: '='
//        },
//        template: "<ul><object ng-repeat='object in list' resource='object' parent='parent'></object></ul>"
//    }
//})

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
            scope.object.path = parentPath + "/" + scope.object.id;
        }
    }
});
