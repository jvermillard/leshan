var lwClientApp = angular.module('lwClientApp', []);

lwClientApp.controller('LwClientCtrl', function LwClientCtrl($scope, $http) {

	// get the list of connected clients
	$http.get('/api/clients').success(function(data, status, headers, config) {
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
});
