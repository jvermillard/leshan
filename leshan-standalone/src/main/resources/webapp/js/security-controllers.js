var lwClientControllers = angular.module('securityControllers', []);

lwClientControllers.controller('SecurityListCtrl', [
    '$scope',
    '$http',
    function SecurityListCtrl($scope, $http) {
    	
    	// update navbar
    	angular.element("#navbar").children().removeClass('active');
    	angular.element("#security-navlink").addClass('active');

        // get the list of security info by end-point
        $http.get('api/security'). error(function(data, status, headers, config){
            $scope.error = "Unable to get the security info list: " + status + " " + data  
            console.error($scope.error)
        }).success(function(data, status, headers, config) {
            $scope.securityInfos = data;        
        });
        
        $scope.newSecurity = function() {
            $('#newSecuritySubmit').unbind();
            $('#newSecuritySubmit').click(function(e){
                e.preventDefault();
                 
                var endpoint = endpointValue.value;
                if(securityMode.value == "psk") {
                	var security = {psk : { identity : pskIdentityValue.value , key : pskValue.value}};
                }
                
                if(endpoint && security) {
                    $('#securityModal').modal('hide');
                    
                    $http({method: 'PUT', url: "api/security/" + endpoint, data: security, headers:{'Content-Type': 'text/plain'}})
                    .success(function(data, status, headers, config) {
                    	
                    	$scope.securityInfos[endpoint] = security;
                        
                   }).error(function(data, status, headers, config) {
                        errormessage = "Unable to add security info for endpoint " + endpoint + " : " + status + " "+ data
                        console.error(errormessage)
                    });
                }
            });

            $('#newSecurityModal').modal('show');
        };
        
}]);
