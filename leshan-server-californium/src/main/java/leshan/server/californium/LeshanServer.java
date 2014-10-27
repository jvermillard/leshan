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
package leshan.server.californium;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import leshan.server.LwM2mServer;
import leshan.server.californium.impl.CaliforniumLwM2mRequestSender;
import leshan.server.californium.impl.LwM2mPskStore;
import leshan.server.californium.impl.RegisterResource;
import leshan.server.californium.impl.SecureEndpoint;
import leshan.server.client.Client;
import leshan.server.client.ClientRegistry;
import leshan.server.client.ClientRegistryListener;
import leshan.server.impl.ClientRegistryImpl;
import leshan.server.impl.ObservationRegistryImpl;
import leshan.server.impl.SecurityRegistryImpl;
import leshan.server.impl.objectspec.Resources;
import leshan.server.observation.ObservationRegistry;
import leshan.server.request.ClientResponse;
import leshan.server.request.ExceptionConsumer;
import leshan.server.request.LwM2mRequest;
import leshan.server.request.ResponseConsumer;
import leshan.server.security.SecurityRegistry;
import leshan.util.Validate;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.scandium.DTLSConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class LeshanServer implements LwM2mServer {

    private final CoapServer coapServer;

    private static final Logger LOG = LoggerFactory.getLogger(LeshanServer.class);

    /** IANA assigned UDP port for CoAP (so for LWM2M) */
    public static final int PORT = 5683;

    /** IANA assigned UDP port for CoAP with DTLS (so for LWM2M) */
    public static final int PORT_DTLS = 5684;

    private final CaliforniumLwM2mRequestSender requestSender;

    private final ClientRegistry clientRegistry;

    private final ObservationRegistry observationRegistry;

    private final SecurityRegistry securityRegistry;

    /**
     * Initialize a server which will bind to default UDP port for CoAP (5684).
     */
    public LeshanServer() {
        this(null, null, null);
    }

    /**
     * Initialize a server which will bind to the specified address and port.
     * 
     * @param localAddress the address to bind the CoAP server.
     * @param localAddressSecure the address to bind the CoAP server for DTLS connection.
     */
    public LeshanServer(InetSocketAddress localAddress, InetSocketAddress localAddressSecure) {
        this(localAddress, localAddressSecure, null, null, null);
    }

    /**
     * Initialize a server which will bind to default UDP port for CoAP (5684).
     */
    public LeshanServer(ClientRegistry clientRegistry, SecurityRegistry securityRegistry,
            ObservationRegistry observationRegistry) {
        this(new InetSocketAddress((InetAddress) null, PORT), new InetSocketAddress((InetAddress) null, PORT_DTLS),
                clientRegistry, securityRegistry, observationRegistry);
    }

    /**
     * Initialize a server which will bind to the specified address and port.
     * 
     * @param localAddress the address to bind the CoAP server.
     * @param localAddressSecure the address to bind the CoAP server for DTLS connection.
     */
    public LeshanServer(InetSocketAddress localAddress, InetSocketAddress localAddressSecure,
            ClientRegistry clientRegistry, SecurityRegistry securityRegistry, ObservationRegistry observationRegistry) {
        Validate.notNull(localAddress, "IP address cannot be null");
        Validate.notNull(localAddressSecure, "Secure IP address cannot be null");

        // init registry
        if (clientRegistry == null)
            this.clientRegistry = new ClientRegistryImpl();
        else
            this.clientRegistry = clientRegistry;

        if (observationRegistry == null)
            this.observationRegistry = new ObservationRegistryImpl();
        else
            this.observationRegistry = observationRegistry;

        if (securityRegistry == null)
            this.securityRegistry = new SecurityRegistryImpl();
        else
            this.securityRegistry = securityRegistry;

        // Cancel observations on client unregistering
        this.clientRegistry.addListener(new ClientRegistryListener() {

            @Override
            public void updated(Client clientUpdated) {
            }

            @Override
            public void unregistered(Client client) {
                LeshanServer.this.observationRegistry.cancelObservations(client);
            }

            @Override
            public void registered(Client client) {
            }
        });

        // init CoAP server
        coapServer = new CoapServer();
        Endpoint endpoint = new CoAPEndpoint(localAddress);
        coapServer.addEndpoint(endpoint);

        // init DTLS server
        DTLSConnector connector = new DTLSConnector(localAddressSecure, null);
        connector.getConfig().setPskStore(new LwM2mPskStore(this.securityRegistry, this.clientRegistry));

        Endpoint secureEndpoint = new SecureEndpoint(connector);
        coapServer.addEndpoint(secureEndpoint);

        // define /rd resource
        RegisterResource rdResource = new RegisterResource(this.clientRegistry, this.securityRegistry);
        coapServer.add(rdResource);

        // create sender
        Set<Endpoint> endpoints = new HashSet<>();
        endpoints.add(endpoint);
        endpoints.add(secureEndpoint);
        requestSender = new CaliforniumLwM2mRequestSender(endpoints, this.observationRegistry);
    }

    /**
     * Starts the server and binds it to the specified port.
     */
    public void start() {
        // load resource definitions
        Resources.load();

        coapServer.start();
        LOG.info("LW-M2M server started");

        // start client registry
        if (clientRegistry instanceof ClientRegistryImpl)
            ((ClientRegistryImpl) clientRegistry).start();
    }

    /**
     * Stops the server and unbinds it from assigned ports (can be restarted).
     */
    public void stop() {
        coapServer.stop();

        if (clientRegistry instanceof ClientRegistryImpl) {
            try {
                ((ClientRegistryImpl) clientRegistry).stop();
            } catch (InterruptedException e) {
                LOG.info("LW-M2M server started");
            }
        }
    }

    /**
     * Stops the server and unbinds it from assigned ports.
     */
    public void destroy() {
        coapServer.destroy();

        if (clientRegistry instanceof ClientRegistryImpl) {
            try {
                ((ClientRegistryImpl) clientRegistry).stop();
            } catch (InterruptedException e) {
                LOG.info("LW-M2M server started");
            }
        }
    }

    @Override
    public ClientRegistry getClientRegistry() {
        return this.clientRegistry;
    }

    @Override
    public ObservationRegistry getObservationRegistry() {
        return this.observationRegistry;
    }

    @Override
    public SecurityRegistry getSecurityRegistry() {
        return this.securityRegistry;
    }

    @Override
    public <T extends ClientResponse> T send(LwM2mRequest<T> request) {
        return requestSender.send(request);
    }

    @Override
    public <T extends ClientResponse> void send(LwM2mRequest<T> request, ResponseConsumer<T> responseCallback,
            ExceptionConsumer errorCallback) {
        requestSender.send(request, responseCallback, errorCallback);
    }
}
