'use strict';

/* App Module */

var leshanApp = angular.module('leshanApp', [ 'ngRoute',
        'lwClientControllers', 'lwResourcesDirective', 'lwResourcesServices', 'securityControllers', 'uiDialogServices', 'ui.bootstrap']);

leshanApp.config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {

    $routeProvider.
        when('/clients', { templateUrl : 'partials/client-list.html', controller : 'ClientListCtrl' }).
        when('/clients/:clientId', { templateUrl : 'partials/client-detail.html', controller : 'ClientDetailCtrl' }).
        when('/security', { templateUrl : 'partials/security-list.html', controller : 'SecurityCtrl' }).
        otherwise({ redirectTo : '/clients' });

} ]);
