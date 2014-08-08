angular.module('instanceDirectives', [])

.directive('instance', function ($compile, $routeParams, $http, dialog,$filter) {
    return {
        restrict: "E",
        replace: true,
        scope: {
            instance: '=',
            parent: '='
        },
        templateUrl: "partials/instance.html",
        link: function (scope, element, attrs) {
            var parentPath = "";
            scope.instance.path = scope.parent.path + "/" + scope.instance.id;
            
            scope.instance.read  =  {tooltip : "Read <br/>"   + scope.instance.path};
            scope.instance.write =  {tooltip : "Write <br/>"  + scope.instance.path};
           
            scope.readable = function() {
                return true;
            }

            scope.writeable = function() {
                return false;
            }
            
            
            scope.read = function() {
                var uri = "api/clients/" + $routeParams.clientId + scope.instance.path;                
                $http.get(uri)
                .success(function(data, status, headers, config) {
                    // manage request information
                    var read = scope.instance.read;
                    read.date = new Date();
                    var formattedDate = $filter('date')(read.date, 'HH:mm:ss.sss');
                    read.status = data.status;
                    read.tooltip = formattedDate + "<br/>" + read.status;
                    
                    // manage read data
                    if (data.status == "CONTENT") {
                        for (var i in data.value) {
                            var res = data.value[i];
                            scope.instance.resources[res.id].value = res.value;
                            scope.instance.resources[res.id].valuesupposed = false;
                            scope.instance.resources[res.id].tooltip = formattedDate;
                        }
                    }
                }).error(function(data, status, headers, config) {
                    if (observe) {
                        scope.instance.observe.status = false;
                    }
                    errormessage = "Unable to read resource " + scope.instance.path + " for "+ $routeParams.clientId + " : " + status +" "+ data
                    dialog.open(errormessage);
                    console.error(errormessage)
                });;
            };
        }
    }
});
