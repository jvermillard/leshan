var myModule = angular.module('lwResourcesServices', []);

myModule.factory('lwResources', function() {
    var serviceInstance = {};
    serviceInstance.buildResourceTree = buildResourceTree;
    serviceInstance.findResource = findResource;
    serviceInstance.addInstance = addInstance;
    serviceInstance.addResource = addResource;
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
            // intance
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
        object = objectDefs[objectId];

        // manage unknown object
        if (object == undefined) {
            object = {
                name : objectId,
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
            object.name = title;
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
            instance.name = title;
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
        // create resource definition
        var resourcedef = {
            name : resourceId,
            id : resourceId,
            operations : "RW"
        };
        object.resourcedefs.push(resourcedef)

        // create resource
        resource = {
            def : resourcedef,
            id : resourceId,
        };
        instance.resources.push(resource);
    }
    if (attributes != undefined) {
        if (attributes.title != undefined) {
            resource.def.name = title;
        } else if (attributes.rt != undefined) {
            resource.def.name = attributes.rt;
        }
    }
    return resource;
}

/**
 * Return model describing the LWM2M Objects defined by OMA
 */
var getObjectDefinitions = function() {
    return [ {
        name : "LWM2M Security",
        id : "0",
        instancetype : "multiple",
        resourcedefs : [ {
            name : "LWM2M Server URI",
            id : "0",
            operations : "W"
        }, {
            name : "Bootstrap Server",
            id : "1",
            operations : "W"
        }, {
            name : "Security Mode",
            id : "2",
            operations : "W"
        }, {
            name : "Public Key or Identity",
            id : "3",
            operations : "W"
        }, {
            name : "Server Public Key or Identity",
            id : "4",
            operations : "W"
        }, {
            name : "Secret Key",
            id : "5",
            operations : "W"
        }, {
            name : "SMS Security Mode",
            id : "6",
            operations : "W"
        }, {
            name : "SMS Binding Key Parameters",
            id : "7",
            operations : "W"
        }, {
            name : "SMS Binding Secret Keys",
            id : "8",
            operations : "W"
        }, {
            name : "LWM2M Server SMS Number",
            id : "9",
            operations : "W"
        }, {
            name : "Short Server ID",
            id : "10",
            operations : "W"
        }, {
            name : "Client Hold Off Time",
            id : "11",
            operations : "W"
        } ]
    }, {
        name : "LWM2M Server",
        id : "1",
        instancetype : "multiple",
        resourcedefs : [ {
            name : "Short Server ID",
            id : "0",
            operations : "R"
        }, {
            name : "Lifetime",
            id : "1",
            operations : "RW"
        }, {
            name : "Default Minimum Period",
            id : "2",
            operations : "RW"
        }, {
            name : "Default Maximum Period",
            id : "3",
            operations : "RW"
        }, {
            name : "Disable",
            id : "4",
            operations : "E"
        }, {
            name : "Disable Timeout",
            id : "5",
            operations : "RW"
        }, {
            name : "Notification Storing When Disabled or Offline",
            id : "6",
            operations : "RW"
        }, {
            name : "Binding",
            id : "7",
            operations : "RW"
        }, {
            name : "Registration of Update Trigger",
            id : "8",
            operations : "E"
        } ]
    }, {
        name : "Access Control",
        id : "2",
        instancetype : "multiple",
        resourcedefs : [ {
            name : "Object ID",
            id : "0",
            operations : "R"
        }, {
            name : "Object Instance ID",
            id : "1",
            operations : "R"
        }, {
            name : "ACL",
            id : "2",
            operations : "RW"
        }, {
            name : "Access Control Owner",
            id : "3",
            operations : "RW"
        } ]
    }, {
        name : "Device",
        id : "3",
        instancetype : "single",
        resourcedefs : [ {
            name : "Manufacturer",
            id : "0",
            operations : "R",
            type : "string"
        }, {
            name : "Model Number",
            id : "1",
            operations : "R",
            type : "string"
        }, {
            name : "Serial Number",
            id : "2",
            operations : "R",
            type : "string"
        }, {
            name : "Firmware Version",
            id : "3",
            operations : "R",
            type : "string"
        }, {
            name : "Reboot",
            id : "4",
            operations : "E"
        }, {
            name : "Factory Reset",
            id : "5",
            operations : "E"
        }, {
            name : "Available Power Sources",
            id : "6",
            operations : "R",
            type : "integer",
            instances : "multiple"
        }, {
            name : "Power Source Voltage",
            id : "7",
            operations : "R",
            type : "integer",
            instances : "multiple"
        }, {
            name : "Power Source Current",
            id : "8",
            operations : "R",
            type : "integer",
            instances : "multiple"
        }, {
            name : "Battery Level",
            id : "9",
            operations : "R",
            type : "integer"
        }, {
            name : "Memory Free",
            id : "10",
            operations : "R",
            type : "integer"
        }, {
            name : "Error Code",
            id : "11",
            operations : "R",
            type : "integer",
            instances : "multiple"
        }, {
            name : "Reset Error Code",
            id : "12",
            operations : "E"
        }, {
            name : "Current Time",
            id : "13",
            operations : "RW",
            type : "time"
        }, {
            name : "UTC Offset",
            id : "14",
            operations : "RW",
            type : "string"
        }, {
            name : "Timezone",
            id : "15",
            operations : "RW",
            type : "string"
        }, {
            name : "Supported Binding and Modes",
            id : "16",
            operations : "R",
            type : "string"
        } ]
    }, {
        name : "Connectivity Monitoring",
        id : "4",
        instancetype : "single",
        resourcedefs : [ {
            name : "Network Bearer",
            id : "0",
            operations : "R"
        }, {
            name : "Available Network Bearer",
            id : "1",
            operations : "R"
        }, {
            name : "Radio Signal Strength",
            id : "2",
            operations : "R"
        }, {
            name : "Link Quality",
            id : "3",
            operations : "R"
        }, {
            name : "IP Addresses",
            id : "4",
            operations : "R"
        }, {
            name : "Router IP Addresse",
            id : "5",
            operations : "R"
        }, {
            name : "Link Utilization",
            id : "6",
            operations : "R"
        }, {
            name : "APN",
            id : "7",
            operations : "R"
        }, {
            name : "Cell ID",
            id : "8",
            operations : "R"
        }, {
            name : "SMNC",
            id : "9",
            operations : "R"
        }, {
            name : "SMCC",
            id : "10",
            operations : "R"
        } ]
    }, {
        name : "Firmware",
        id : "5",
        instancetype : "single",
        resourcedefs : [ {
            name : "Package",
            id : "0",
            operations : "W",
            type : "opaque"
        }, {
            name : "Package URI",
            id : "1",
            operations : "W",
            type : "string"
        }, {
            name : "Update",
            id : "2",
            operations : "E"
        }, {
            name : "State",
            id : "3",
            operations : "R",
            type : "integer"
        }, {
            name : "Update Supported Objects",
            id : "4",
            operations : "RW",
            type : "boolean"
        }, {
            name : "Update Result",
            id : "5",
            operations : "R",
            type : "integer"
        } ]
    }, {
        name : "Location",
        id : "6",
        instancetype : "single",
        resourcedefs : [ {
            name : "Latitude",
            id : "0",
            operations : "R"
        }, {
            name : "Longitude",
            id : "1",
            operations : "R"
        }, {
            name : "Altitude",
            id : "2",
            operations : "R"
        }, {
            name : "Uncertainty",
            id : "3",
            operations : "R"
        }, {
            name : "Velocity",
            id : "4",
            operations : "R"
        }, {
            name : "Timestamp",
            id : "5",
            operations : "R"
        } ]
    }, {
        name : "Connectivity Statistics",
        id : "7",
        instancetype : "single",
        resourcedefs : [ {
            name : "SMS Tx Counter",
            id : "0",
            operations : "R"
        }, {
            name : "SMS Rx Counter",
            id : "1",
            operations : "R"
        }, {
            name : "Tx Data",
            id : "2",
            operations : "R"
        }, {
            name : "Rx Data",
            id : "3",
            operations : "R"
        }, {
            name : "Max Message Size",
            id : "4",
            operations : "R"
        }, {
            name : "Average Message Size",
            id : "5",
            operations : "R"
        }, {
            name : "StartOrReset",
            id : "6",
            operations : "E"
        } ]
    } ]
}