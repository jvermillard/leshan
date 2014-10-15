/*!
 * Copyright (c) 2013-2014, Sierra Wireless
 * Released under the BSD license
 * https://raw.githubusercontent.com/jvermillard/leshan/master/LICENSE
 */

var myModule = angular.module('lwResourcesServices', []);

myModule.factory('lwResources', function() {
    var serviceInstance = {};
    serviceInstance.buildResourceTree = buildResourceTree;
    serviceInstance.findResource = findResource;
    serviceInstance.addInstance = addInstance;
    serviceInstance.addResource = addResource;
    serviceInstance.getTypedValue = getTypedValue;
    return serviceInstance;
});

/**
 * Get array from url string (e.g. : "/3/0/1" => [3,0,1])
 */
var url2array = function(url) {
    if (url.length > 0 && url.charAt(0) === '/') {
        url = url.substr(1);
    }
    return url.split("/");
}

/**
 * Search an element in an array by id
 */
var searchById = function(array, id) {
    for (i in array) {
        var elem = array[i]
        if (elem.id == id) {
            return elem;
        }
    }
    return null;
}

/**
 * Search a resource in the given tree
 */
var findResource = function(tree, url) {
    var resourcepath = url2array(url);

    if (resourcepath.length == 3) {
        var object = searchById(tree, resourcepath[0]);
        if (object != undefined) {
            var instance = searchById(object.instances, resourcepath[1])
            if (instance != undefined) {
                return searchById(instance.resources, resourcepath[2])
            }
        }
    }
    return null;
}

/**
 * Build Resource Tree for the given objectLinks
 */
var buildResourceTree = function(objectLinks) {
    if (objectLinks.length == 0)
        return [];

    var tree = [];
    var objectDefs = getObjectDefinitions();

    for (var i = 0; i < objectLinks.length; i++) {
        // get list of resource (e.g. : [3] or [1,123]
        var resourcepath = url2array(objectLinks[i].url);
        var attributes = objectLinks[i].attributes;

        switch (resourcepath.length) {
        case 1:
            // object
            var object = addObject(tree, objectDefs, resourcepath[0],
                    attributes)

            // manage single instance
            if (object.instancetype != "multiple") {
                addInstance(object, 0, null)
            }

            break;
        case 2:
            // instance
            var object = addObject(tree, objectDefs, resourcepath[0], null)
            addInstance(object, resourcepath[1], attributes)

            break;
        case 3:
        default:
            // resource
            var object = addObject(tree, objectDefs, resourcepath[0], null)
            var instance = addInstance(object, resourcepath[1], null)
            addResource(object, instance, resourcepath[2], attributes)

            break;
        }
    }
    return tree;
}

/**
 * add object with the given ID to resource tree if necessary and return it
 */
var addObject = function(tree, objectDefs, objectId, attributes) {
    var object = searchById(tree, objectId);

    // if object is not already in the tree
    if (object == undefined) {
        // search object definition for this id
        object = searchById(objectDefs, objectId);

        // manage unknown object
        if (object == undefined) {
            object = {
                name : "Object " + objectId,
                id : objectId,
                instancetype : "multiple",
                resourcedefs : []
            };
        }

        // add instances field to this object
        object.instances = [];

        // add object to tree
        tree.push(object);
    }
    if (attributes != undefined) {
        if (attributes.title != undefined) {
            object.name = attributes.title;
        } else if (attributes.rt != undefined) {
            object.name = attributes.rt;
        }
    }
    return object;
}

/**
 * add instance with the given ID to resource tree if necessary and return it
 */
var addInstance = function(object, instanceId, attributes) {
    var instance = searchById(object.instances, instanceId);

    // create instance if necessary
    if (instance == undefined) {
        instance = {
            name : "Instance " + instanceId,
            id : instanceId,
            resources : []
        };

        for (j in object.resourcedefs) {
            var resourcedef = object.resourcedefs[j]
            instance.resources.push({
                def : resourcedef,
                id : resourcedef.id
            });
        }
        object.instances.push(instance);
    }
    if (attributes != undefined) {
        if (attributes.title != undefined) {
            instance.name = attributes.title;
        } else if (attributes.rt != undefined) {
            instance.name = attributes.rt;
        }
    }
    return instance;
}

/**
 * add resource with the given ID to resource tree if necessary and return it
 */
var addResource = function(object, instance, resourceId, attributes) {
    var resource = searchById(instance.resources, resourceId);

    // create resource if necessary
    if (resource == undefined) {
        // create resource definition if necessary
        var resourcedef = searchById(object.resourcedefs, resourceId);
        if (resourcedef == undefined){
            var resourcedef = {
                name : "Resource " + resourceId,
                id : resourceId,
                operations : "RW"
            };
            object.resourcedefs.push(resourcedef)
        }

        // create resource
        resource = {
            def : resourcedef,
            id : resourceId,
        };
        instance.resources.push(resource);
    }
    if (attributes != undefined) {
        if (attributes.title != undefined) {
            resource.def.name = attributes.title;
        } else if (attributes.rt != undefined) {
            resource.def.name = attributes.rt;
        }
    }
    return resource;
}

var getTypedValue = function(strValue, type) {
    var val = strValue;
    if(type != undefined) {
        switch(type) {
            case "integer":
                val = parseInt(strValue);
                break;
            case "float":
                val = parseFloat(strValue);
                break;
            default:
                val = strValue;   
        }
    }
    return val;
}

/**
 * Return model describing the LWM2M Objects defined by OMA
 */
var getObjectDefinitions = function() {
    return [
            {
                "name": "LWM2M Security",
                "id": 0,
                "instancetype": "mutiple",
                "mandatory": true,
                "description": "",
                "resourcedefs": [
                  {
                    "id": 0,
                    "name": "LWM2M  Server URI",
                    "operations": "NONE",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "string",
                    "range": "0-255 bytes",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 1,
                    "name": "Bootstrap Server",
                    "operations": "NONE",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "boolean",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 2,
                    "name": "Security Mode",
                    "operations": "NONE",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "integer",
                    "range": "0-3",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 3,
                    "name": "Public Key or Identity",
                    "operations": "NONE",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "opaque",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 4,
                    "name": "Server Public Key or Identity",
                    "operations": "NONE",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "opaque",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 5,
                    "name": "Secret Key",
                    "operations": "NONE",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "opaque",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 6,
                    "name": "SMS Security Mode",
                    "operations": "NONE",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "integer",
                    "range": "0-255",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 7,
                    "name": "SMS Binding Key Parameters",
                    "operations": "NONE",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "opaque",
                    "range": "6 bytes",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 8,
                    "name": "SMS Binding Secret Keys",
                    "operations": "NONE",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "opaque",
                    "range": "32-48 bytes",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 9,
                    "name": "LWM2M Server SMS Number",
                    "operations": "NONE",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "integer",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 10,
                    "name": "Short Server ID",
                    "operations": "NONE",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "integer",
                    "range": "1-65535",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 11,
                    "name": "Client Hold Off Time",
                    "operations": "NONE",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "integer",
                    "range": "",
                    "units": "s",
                    "description": ""
                  }
                ]
              },
              {
                "name": "LWM2M Server",
                "id": 1,
                "instancetype": "mutiple",
                "mandatory": true,
                "description": "",
                "resourcedefs": [
                  {
                    "id": 0,
                    "name": "Short Server ID",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "integer",
                    "range": "1-65535",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 1,
                    "name": "Lifetime",
                    "operations": "RW",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "integer",
                    "range": "",
                    "units": "s",
                    "description": ""
                  },
                  {
                    "id": 2,
                    "name": "Default Minimum Period",
                    "operations": "RW",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "integer",
                    "range": "",
                    "units": "s",
                    "description": ""
                  },
                  {
                    "id": 3,
                    "name": "Default Maximum Period",
                    "operations": "RW",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "integer",
                    "range": "",
                    "units": "s",
                    "description": ""
                  },
                  {
                    "id": 4,
                    "name": "Disable",
                    "operations": "E",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "string",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 5,
                    "name": "Disable Timeout",
                    "operations": "RW",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "integer",
                    "range": "",
                    "units": "s",
                    "description": ""
                  },
                  {
                    "id": 6,
                    "name": "Notification Storing When Disabled or Offline",
                    "operations": "RW",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "boolean",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 7,
                    "name": "Binding",
                    "operations": "RW",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "string",
                    "range": "The possible values of Resource are listed in 5.2.1.1",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 8,
                    "name": "Registration Update Trigger",
                    "operations": "E",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "string",
                    "range": "",
                    "units": "",
                    "description": ""
                  }
                ]
              },
              {
                "name": "LWM2M Access Control",
                "id": 2,
                "instancetype": "mutiple",
                "mandatory": false,
                "description": "",
                "resourcedefs": [
                  {
                    "id": 0,
                    "name": "Object ID",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "integer",
                    "range": "1-65534",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 1,
                    "name": "Object Instance ID",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "integer",
                    "range": "0-65535",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 2,
                    "name": "ACL",
                    "operations": "RW",
                    "instancetype": "mutiple",
                    "mandatory": false,
                    "type": "integer",
                    "range": "16-bit",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 3,
                    "name": "Access Control Owner",
                    "operations": "RW",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "integer",
                    "range": "0-65535",
                    "units": "",
                    "description": ""
                  }
                ]
              },
              {
                "name": "Device",
                "id": 3,
                "instancetype": "single",
                "mandatory": true,
                "description": "",
                "resourcedefs": [
                  {
                    "id": 0,
                    "name": "Manufacturer",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "string",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 1,
                    "name": "Model Number",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "string",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 2,
                    "name": "Serial Number",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "string",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 3,
                    "name": "Firmware Version",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "string",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 4,
                    "name": "Reboot",
                    "operations": "E",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "string",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 5,
                    "name": "Factory Reset",
                    "operations": "E",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "string",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 6,
                    "name": "Available Power Sources",
                    "operations": "R",
                    "instancetype": "mutiple",
                    "mandatory": false,
                    "type": "integer",
                    "range": "0-7",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 7,
                    "name": "Power Source Voltage",
                    "operations": "R",
                    "instancetype": "mutiple",
                    "mandatory": false,
                    "type": "integer",
                    "range": "",
                    "units": "mV",
                    "description": ""
                  },
                  {
                    "id": 8,
                    "name": "Power Source Current",
                    "operations": "R",
                    "instancetype": "mutiple",
                    "mandatory": false,
                    "type": "integer",
                    "range": "",
                    "units": "mA",
                    "description": ""
                  },
                  {
                    "id": 9,
                    "name": "Battery Level",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "integer",
                    "range": "0-100",
                    "units": "%",
                    "description": ""
                  },
                  {
                    "id": 10,
                    "name": "Memory Free",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "integer",
                    "range": "",
                    "units": "KB",
                    "description": ""
                  },
                  {
                    "id": 11,
                    "name": "Error Code",
                    "operations": "R",
                    "instancetype": "mutiple",
                    "mandatory": true,
                    "type": "integer",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 12,
                    "name": "Reset Error Code",
                    "operations": "E",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "string",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 13,
                    "name": "Current Time",
                    "operations": "RW",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "time",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 14,
                    "name": "UTC Offset",
                    "operations": "RW",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "string",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 15,
                    "name": "Timezone",
                    "operations": "RW",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "string",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 16,
                    "name": "Supported Binding and Modes",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "string",
                    "range": "",
                    "units": "",
                    "description": ""
                  }
                ]
              },
              {
                "name": "Connectivity Monitoring",
                "id": 4,
                "instancetype": "single",
                "mandatory": false,
                "description": "",
                "resourcedefs": [
                  {
                    "id": 0,
                    "name": "Network Bearer",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "integer",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 1,
                    "name": "Available Network Bearer",
                    "operations": "R",
                    "instancetype": "mutiple",
                    "mandatory": true,
                    "type": "integer",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 2,
                    "name": "Radio Signal Strength",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "integer",
                    "range": "",
                    "units": "dBm",
                    "description": ""
                  },
                  {
                    "id": 3,
                    "name": "Link Quality",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "integer",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 4,
                    "name": "IP Addresses",
                    "operations": "R",
                    "instancetype": "mutiple",
                    "mandatory": true,
                    "type": "string",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 5,
                    "name": "Router IP Addresse",
                    "operations": "R",
                    "instancetype": "mutiple",
                    "mandatory": false,
                    "type": "string",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 6,
                    "name": "Link Utilization",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "integer",
                    "range": "0-100",
                    "units": "%",
                    "description": ""
                  },
                  {
                    "id": 7,
                    "name": "APN",
                    "operations": "R",
                    "instancetype": "mutiple",
                    "mandatory": false,
                    "type": "string",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 8,
                    "name": "Cell ID",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "integer",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 9,
                    "name": "SMNC",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "integer",
                    "range": "",
                    "units": "%",
                    "description": ""
                  },
                  {
                    "id": 10,
                    "name": "SMCC",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "integer",
                    "range": "",
                    "units": "",
                    "description": ""
                  }
                ]
              },
              {
                "name": "Firmware Update",
                "id": 5,
                "instancetype": "single",
                "mandatory": false,
                "description": "",
                "resourcedefs": [
                  {
                    "id": 0,
                    "name": "Package",
                    "operations": "W",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "opaque",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 1,
                    "name": "Package URI",
                    "operations": "W",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "string",
                    "range": "0-255 bytes",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 2,
                    "name": "Update",
                    "operations": "E",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "string",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 3,
                    "name": "State",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "integer",
                    "range": "1-3",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 4,
                    "name": "Update Supported Objects",
                    "operations": "RW",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "boolean",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 5,
                    "name": "Update Result",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "integer",
                    "range": "0-6",
                    "units": "",
                    "description": ""
                  }
                ]
              },
              {
                "name": "Location",
                "id": 6,
                "instancetype": "single",
                "mandatory": false,
                "description": "",
                "resourcedefs": [
                  {
                    "id": 0,
                    "name": "Latitude",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "string",
                    "range": "",
                    "units": "Deg",
                    "description": ""
                  },
                  {
                    "id": 1,
                    "name": "Longitude",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "string",
                    "range": "",
                    "units": "Deg",
                    "description": ""
                  },
                  {
                    "id": 2,
                    "name": "Altitude",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "string",
                    "range": "",
                    "units": "m",
                    "description": ""
                  },
                  {
                    "id": 3,
                    "name": "Uncertainty",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "string",
                    "range": "",
                    "units": "m",
                    "description": ""
                  },
                  {
                    "id": 4,
                    "name": "Velocity",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "opaque",
                    "range": "",
                    "units": "Refers to 3GPP GAD specs",
                    "description": ""
                  },
                  {
                    "id": 5,
                    "name": "Timestamp",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "time",
                    "range": "0-6",
                    "units": "",
                    "description": ""
                  }
                ]
              },
              {
                "name": "Connectivity Statistics",
                "id": 7,
                "instancetype": "single",
                "mandatory": false,
                "description": "",
                "resourcedefs": [
                  {
                    "id": 0,
                    "name": "SMS Tx Counter",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "integer",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 1,
                    "name": "SMS Rx Counter",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "integer",
                    "range": "",
                    "units": "",
                    "description": ""
                  },
                  {
                    "id": 2,
                    "name": "Tx Data",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "integer",
                    "range": "",
                    "units": "Kilo-Bytes",
                    "description": ""
                  },
                  {
                    "id": 3,
                    "name": "Rx Data",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "integer",
                    "range": "",
                    "units": "Kilo-Bytes",
                    "description": ""
                  },
                  {
                    "id": 4,
                    "name": "Max Message Size",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "integer",
                    "range": "",
                    "units": "Byte",
                    "description": ""
                  },
                  {
                    "id": 5,
                    "name": "Average Message Size",
                    "operations": "R",
                    "instancetype": "single",
                    "mandatory": false,
                    "type": "integer",
                    "range": "",
                    "units": "Byte",
                    "description": ""
                  },
                  {
                    "id": 6,
                    "name": "StartOrReset",
                    "operations": "E",
                    "instancetype": "single",
                    "mandatory": true,
                    "type": "string",
                    "range": "",
                    "units": "",
                    "description": ""
                  }
                ]
              }
            ]
}