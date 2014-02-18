var lwClientControllers = angular.module('lwClientControllers', []);

lwClientControllers.controller('ClientListCtrl', [
		'$scope',
		'$http',
		function ClientListCtrl($scope, $http) {

			// get the list of connected clients
			$http.get('/api/clients').success(
					function(data, status, headers, config) {
						$scope.clients = data;
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

			// get the lw resources description (static for now)
			$http.get('/json/lw-resources.json').success(
				function(data, status, headers, config) {
					$scope.lwresources = data;
				}
			);
			
		} ]);
