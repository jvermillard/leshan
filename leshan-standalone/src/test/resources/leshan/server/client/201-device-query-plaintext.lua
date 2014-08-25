local lwm2m = require 'lwm2m'
local socket = require 'socket'
local dtls = require 'dtls'
local obj = require 'lwm2mobject'

local udp = socket.udp();
-- let the system choose an ephemeral port
-- udp:setsockname('*', 5682)

-- change UDP socket in DTLS socket
dtls.wrap(udp, {security = "PSK", identity = arg[1], key = arg[2]})

local server = os.getenv("SERVER_HOST")
local endpoint = os.getenv("ENDPOINT")

local deviceObj = obj.new(3, {
  [0]  = "Open Mobile Alliance",                                       -- manufacturer
  [1]  = "Leshan model",                                               -- model number
  [2]  = "458662135557",                                               -- serial number
  [3]  = "1.0",                                                        -- firmware version
  [4]  = {execute = function (obj) print ("Reboot!") end},             -- reboot   
  [5]  = {execute = function (obj) print ("Factory reset!") end},      -- factory reset                                  
  [13] = {read = function() return os.time() end},                     -- current time
})

print("endpoint: " .. endpoint .. " - server host: " .. server);

local ll = lwm2m.init(endpoint, {deviceObj},
  function(data,host,port) udp:sendto(data,host,port) end)

ll:addserver(123, server, 5684)
ll:register()

repeat
  ll:step()
  local data, ip, port, msg = udp:receivefrom()
  if data then
    ll:handle(data,ip,port)
  end
until false
