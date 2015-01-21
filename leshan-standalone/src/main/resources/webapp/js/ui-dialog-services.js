/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/

var myModule = angular.module('uiDialogServices', []);

myModule.factory('dialog', function() {
  var serviceInstance = {};
  
  serviceInstance.open = function (message) {
      $('#messageModalLabel').text(message);
      $('#messageModal').modal('show');
  }
  return serviceInstance;
});