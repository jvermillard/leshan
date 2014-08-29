/*!
 * Copyright (c) 2013-2014, Sierra Wireless
 * Released under the BSD license
 */

var myModule = angular.module('uiDialogServices', []);

myModule.factory('dialog', function() {
  var serviceInstance = {};
  
  serviceInstance.open = function (message) {
      $('#messageModalLabel').text(message);
      $('#messageModal').modal('show');
  }
  return serviceInstance;
});