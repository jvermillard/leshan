'use strict';

/* App Module */

var lwClientApp = angular.module('lwClientApp', [ 'ngRoute',
        'lwClientControllers', 'lwResourcesDirective', 'lwResourcesServices', 'ui.bootstrap']);

lwClientApp.config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {

    $routeProvider.
        when('/clients', { templateUrl : 'partials/client-list.html', controller : 'ClientListCtrl' }).
        when('/clients/:clientId', { templateUrl : 'partials/client-detail.html', controller : 'ClientDetailCtrl' }).
        otherwise({ redirectTo : '/clients' });

} ]);
