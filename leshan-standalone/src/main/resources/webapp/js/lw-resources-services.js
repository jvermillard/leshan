var myModule = angular.module('lwResourcesServices', []);

myModule.factory('lwResources', function() {
  var serviceInstance = {};
  serviceInstance.getModel = getModel;
  return serviceInstance;
});

var getModel = function(){
    return [
        {
            name   : "LWM2M Security",
            id     : "0",
            values : [
                {
                    name   : "Instance 0",
                    id     : "0",
                    values : [
                        {
                            name : "LWM2M Server URI",
                            id   : "0"
                        },
                        {
                            name : "Bootstrap Server",
                            id   : "1"
                        },
                        {
                            name : "Security Mode",
                            id   : "2"
                        },
                        {
                            name : "Public Key or Identity",
                            id   : "3"
                        },
                        {
                            name : "Server Public Key or Identity",
                            id   : "4"
                        },
                        {
                            name : "Secret Key",
                            id   : "5"
                        },
                        {
                            name : "Short Server ID",
                            id   : "6"
                        },
                        {
                            name : "Client Hold Off Time",
                            id   : "7"
                        }
                    ]
                }
            ]
        },
        {
            name   : "LWM2M Server",
            id     : "1",
            type   : "object",
            values : [
                {
                    name   : "Instance 0",
                    id     : "0",
                    values : [
                        {
                            name : "Short Server ID",
                            id   : "0"
                        },
                        {
                            name : "Lifetime",
                            id   : "1"
                        },
                        {
                            name : "Default Minimum Period",
                            id   : "2"
                        },
                        {
                            name : "Default Maximum Period",
                            id   : "3"
                        },
                        {
                            name : "Disable",
                            id   : "4"
                        },
                        {
                            name : "Disable Timeout",
                            id   : "5"
                        },
                        {
                            name : "Notification Storing When Disabled or Offline",
                            id   : "6"
                        },
                        {
                            name : "Binding",
                            id   : "7"
                        },
                        {
                            name : "Registration of Update Trigger",
                            id   : "8"
                        }
                    ]
                }
            ]
        },
        {
            name    : "Access Control",
            id      : "2",
            values  : [
                {
                    name   : "Instance 0",
                    id     : "0",
                    values : [
                        {
                            name : "Object ID",
                            id   : "0"
                        },
                        {
                            name : "Object Instance ID",
                            id   : "1"
                        },
                        {
                            name : "ACL",
                            id   : "2"
                        },
                        {
                            name : "Access Control Owner",
                            id   : "3"
                        }
                    ]
                }
            ]
        },
        {
            name   : "Device",
            id     : "3",
            values : [
                {
                    name   : "Instance 0",
                    id     : "0",
                    values : [
                        {
                            name       :"Manufacturer",
                            id         : "0",
                            operations : "R",
                            type       : "string"
                        },
                        {
                            name       : "Model Number",
                            id         : "1",
                            operations : "R",
                            type       : "string"
                        },
                        {
                            name       : "Serial Number",
                            id         : "2",
                            operations : "R",
                            type       : "string"
                        },
                        {
                            name       : "Firmware Version",
                            id         : "3",
                            operations : "R",
                            type       : "string"
                        },
                        {
                            name       : "Reboot",
                            id         : "4",
                            operations : "E"
                        },
                        {
                            name       : "Factory Reset",
                            id         : "5",
                            operations : "E"
                        },
                        {
                            name       : "Available Power Sources",
                            id         : "6",
                            operations : "R",
                            type       : "integer",
                            instances  : "multiple"
                        },
                        {
                            name       : "Power Source Voltage",
                            id         : "7",
                            operations : "R",
                            type       : "integer",
                            instances  : "multiple"
                        },
                        {
                            name       : "Power Source Current",
                            id         : "8",
                            operations : "R",
                            type       : "integer",
                            instances  : "multiple"
                        },
                        {
                            name       : "Battery Level",
                            id         : "9",
                            operations : "R",
                            type       : "integer"
                        },
                        {
                            name       : "Memory Free",
                            id         : "10",
                            operations : "R",
                            type       : "integer"
                        },
                        {
                            name       : "Error Code",
                            id         : "11",
                            operations : "R",
                            type       : "integer",
                            instances  : "multiple"
                        },
                        {
                            name       : "Reset Error Code",
                            id         : "12",
                            operations : "E"
                        },
                        {
                            name       : "Current Time",
                            id         : "13",
                            operations : "RW",
                            type       : "time"
                        },
                        {
                            name       : "UTC Offset",
                            id         : "14",
                            operations : "RW",
                            type       : "string"
                        },
                        {
                            name       : "Timezone",
                            id         : "15",
                            operations : "RW",
                            type       : "string"
                        },
                        {
                            name       : "Supported Binding and Modes",
                            id         : "16",
                            operations : "R",
                            type       : "string"
                        }
                    ]
                }
            ]
        },
        {
            name   : "Connectivity Monitoring",
            id     : "4",
            values : [
                {
                    name   : "Instance 0",
                    id     : "0",
                    values : []
                }
            ]
        },
        {
            name   : "Firmware",
            id     : "5",
            values : [
                {
                    name   : "Instance 0",
                    id     : "0",
                    values : [
                        {
                            name       : "Package",
                            id         : "0",
                            operations : "W",
                            type       : "opaque"
                        },
                        {
                            name       : "Package URI",
                            id         : "1",
                            operations : "W",
                            type       : "string"
                        },
                        {
                            name       : "Update",
                            id         : "2",
                            operations : "E"
                        },
                        {
                            name       : "State",
                            id         : "3",
                            operations : "R",
                            type       : "integer"
                        },
                        {
                            name       : "Update Supported Objects",
                            id         : "4",
                            operations : "RW",
                            type       : "boolean"
                        },
                        {
                            name       : "Update Result",
                            id         : "5",
                            operations : "R",
                            type       : "integer"
                        }
                    ]
                }
            ]
        },
        {
            name   : "Location",
            id     : "6",
            values : [
                {
                    name   : "Instance 0",
                    id     : "0",
                    values : []
                }
            ]
        },
        {
            name   : "Connectivity Statistics",
            id     : "7",
            values : [
                {
                    name   : "Instance 0",
                    id     : "0",
                    values : []
                }
            ]
        }
    ]
}