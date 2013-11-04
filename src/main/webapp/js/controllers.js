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

			// listen for new connections
			var handleCallback = function(msg) {
				$scope.$apply(function() {
					var client = JSON.parse(msg.data);
					$scope.clients.push(client);
				});
			}

			var source = new EventSource('/event');
			source.addEventListener('REGISTRATION', handleCallback, false);
		} ]);

lwClientControllers.controller('ClientDetailCtrl', [ '$scope', '$location', '$anchorScroll', '$routeParams', '$http',
		function($scope, $location, $anchorScroll, $routeParams, $http) {
			$scope.clientId = $routeParams.clientId;
			
			$scope.scrollTo = function(id) {
			     $location.hash(id);
			     $anchorScroll();
			  };
			  
			// get the lw resources description (static for now)
			$http.get('/json/lw-resources.json').success(
				function(data, status, headers, config) {
					$scope.lwresources = data;
				});
			
			$scope.read = function(resource, idx) {
				
				// TODO read for full object
				$http.get("/api/clients/" + $scope.clientId + "/" + resource)
					.success(function(data, status, headers, config) {
						//alert(JSON.stringify(data));
						if(data.status = "CONTENT") {
							$scope.lwresources[idx].value = data.value;
						}
					});
				};
			
		} ]);