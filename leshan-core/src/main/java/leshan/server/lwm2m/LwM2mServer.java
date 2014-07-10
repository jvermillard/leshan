/*
 * Copyright (c) 2013, Sierra Wireless
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of {{ project }} nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package leshan.server.lwm2m;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.message.RequestHandler;
import leshan.server.lwm2m.message.californium.CaliforniumBasedRequestHandler;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.resource.RegisterResource;
import leshan.server.lwm2m.security.SecureEndpoint;
import leshan.server.lwm2m.security.SecurityRegistry;
import leshan.server.lwm2m.security.SecurityRegistryImpl;

import org.apache.commons.lang.Validate;
import org.eclipse.californium.scandium.DTLSConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.network.CoAPEndpoint;
import ch.ethz.inf.vs.californium.network.Endpoint;
import ch.ethz.inf.vs.californium.server.Server;

/**
 * A Lightweight M2M server.
 * <p>
 * This CoAP server defines a /rd resources as described in the CoRE RD specification. A {@link ClientRegistry} must be
 * provided to host the description of all the registered LW-M2M clients.
 * </p>
 * <p>
 * A {@link RequestHandler} is provided to perform server-initiated requests to LW-M2M clients.
 * </p>
 */
public class LwM2mServer {

    private final Server coapServer;

    private static final Logger LOG = LoggerFactory.getLogger(LwM2mServer.class);

    /** IANA assigned UDP port for CoAP (so for LWM2M) */
    public static final int PORT = 5683;

    /** IANA assigned UDP port for CoAP with DTLS (so for LWM2M) */
    public static final int PORT_DTLS = 5684;

    private final RequestHandler requestHandler;

    /**
     * Initialize a server which will bind to default UDP port for CoAP (5684).
     * 
     * @param clientRegistry the client registry
     */
    public LwM2mServer(ClientRegistry clientRegistry) {
        this(new InetSocketAddress((InetAddress) null, PORT), new InetSocketAddress((InetAddress) null, PORT_DTLS),
                clientRegistry, null, null);
    }

    /**
     * Initialize a server which will bind to default UDP port for CoAP (5684).
     * 
     * @param clientRegistry the client registry
     */
    public LwM2mServer(ClientRegistry clientRegistry, SecurityRegistry securityRegistry,
            ObservationRegistry observationRegistry) {
        this(new InetSocketAddress((InetAddress) null, PORT), new InetSocketAddress((InetAddress) null, PORT_DTLS),
                clientRegistry, securityRegistry, observationRegistry);
    }

    /**
     * Initialize a server which will bind to the specified address and port.
     * 
     * @param localAddress the address to bind the CoAP server.
     * @param clientRegistry the client registry
     */
    public LwM2mServer(InetSocketAddress localAddress, InetSocketAddress localAddressSecure,
            ClientRegistry clientRegistry) {

        this(localAddress, localAddressSecure, clientRegistry, null, null);
    }

    /**
     * Initialize a server which will bind to the specified address and port.
     * 
     * @param localAddress the address to bind the CoAP server.
     * @param clientRegistry the client registry
     */
    public LwM2mServer(InetSocketAddress localAddress, InetSocketAddress localAddressSecure,
            ClientRegistry clientRegistry, SecurityRegistry securityRegistry, ObservationRegistry observationRegistry) {
        Validate.notNull(clientRegistry, "Client registry must not be null");
        Validate.notNull(localAddress, "IP address cannot be null");

        // init CoAP server
        coapServer = new Server();
        Endpoint endpoint = new CoAPEndpoint(localAddress);
        coapServer.addEndpoint(endpoint);

        // init DTLS server

        if (securityRegistry == null) {
            securityRegistry = new SecurityRegistryImpl();
        }

        Endpoint secureEndpoint = new SecureEndpoint(new DTLSConnector(localAddressSecure, securityRegistry));
        coapServer.addEndpoint(secureEndpoint);

        // define /rd resource
        RegisterResource rdResource = new RegisterResource(clientRegistry, securityRegistry);
        coapServer.add(rdResource);

        Set<Endpoint> endpoints = new HashSet<>();
        endpoints.add(endpoint);
        endpoints.add(secureEndpoint);
        CaliforniumBasedRequestHandler handler = new CaliforniumBasedRequestHandler(endpoints, observationRegistry, 0);
        // register the request handler as listener in order to cancel
        // observations and free up resources when clients unregister
        clientRegistry.addListener(handler);
        requestHandler = handler;
    }

    /**
     * Starts the server and binds it to the specified port.
     */
    public void start() {
        coapServer.start();
        LOG.info("LW-M2M server started");
    }

    /**
     * Stops the server and unbinds it from assigned port.
     */
    public void stop() {
        coapServer.stop();
    }

    public RequestHandler getRequestHandler() {
        return requestHandler;
    }

}
