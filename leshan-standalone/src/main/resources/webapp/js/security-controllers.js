/*!
 * Copyright (c) 2013-2014, Sierra Wireless
 * Released under the BSD license
 */

angular.module('securityControllers', [])

.controller('SecurityCtrl', [
    '$scope',
    '$http',
    'dialog',
    function SecurityCtrl($scope, $http, dialog) {
    	
    	// update navbar
    	angular.element("#navbar").children().removeClass('active');
    	angular.element("#security-navlink").addClass('active');

        // get the list of security info by end-point
        $http.get('api/security'). error(function(data, status, headers, config){
            $scope.error = "Unable to get the security info list: " + status + " " + data  
            console.error($scope.error)
        }).success(function(data, status, headers, config) {
        	$scope.securityInfos = {}
        	for (var i = 0; i < data.length; i++) {
        		$scope.securityInfos[data[i].endpoint] = data[i];
        	}
        });
        
        $scope.remove = function(endpoint) {
        	$http({method: 'DELETE', url: "api/security/" + endpoint, headers:{'Content-Type': 'text/plain'}})
            .success(function(data, status, headers, config) {
            	
            	delete $scope.securityInfos[endpoint];
                
           }).error(function(data, status, headers, config) {
                errormessage = "Unable to remove security info for endpoint " + endpoint + ": " + status + " - " + data;
                dialog.open(errormessage);
                console.error(errormessage);
            });
        }
        
        $scope.save = function() {
            
            $scope.$broadcast('show-errors-check-validity');
            
            if ($scope.form.$valid) {
            	
              if($scope.securityMode == "psk") {
            	  var security = {endpoint: $scope.endpoint, psk : { identity : $scope.pskIdentity , key : $scope.pskValue}};
              }
              else {
            	  dialog.open("RPK not supported yet");
              }
              
              if(security) {
	              $http({method: 'PUT', url: "api/security", data: security, headers:{'Content-Type': 'text/plain'}})
	                .success(function(data, status, headers, config) {
	                	$scope.securityInfos[$scope.endpoint] = security;
	                	$('#newSecurityModal').modal('hide');
	                    
	              }).error(function(data, status, headers, config) {
	                    errormessage = "Unable to add security info for endpoint " + $scope.endpoint + ": " + status + " - " + data;
	                    dialog.open(errormessage);
	                    console.error(errormessage)
	              });
	          }
            }
        }
    
	    $scope.showModal = function() {
	    	$('#newSecurityModal').modal('show');
	    	$scope.$broadcast('show-errors-reset');
	        $scope.endpoint = ''
	        $scope.securityMode = 'psk'
	        $scope.pskIdentity = ''
	        $scope.pskValue = ''
	        $scope.rskValue = ''
	    }
        
}])


/* directive to toggle error class on input fields */
.directive('showErrors', function($timeout) {
	return {
		restrict : 'A',
		require : '^form',
		link : function(scope, el, attrs, formCtrl) {
			// find the text box element, which has the 'name' attribute
			var inputEl = el[0].querySelector("[name]");
			// convert the native text box element to an angular element
			var inputNgEl = angular.element(inputEl);
			// get the name on the text box
			var inputName = inputNgEl.attr('name');
			
			// only apply the has-error class after the user leaves the text box
			inputNgEl.bind('blur', function() {
				el.toggleClass('has-error', formCtrl[inputName].$invalid);
			});

			scope.$on('show-errors-check-validity', function() {
				el.toggleClass('has-error', formCtrl[inputName].$invalid);
			});

			scope.$on('show-errors-reset', function() {
				$timeout(function() {
					el.removeClass('has-error');
				}, 0, false);
			});
		}
	}
})


