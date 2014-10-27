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
package leshan.server.californium.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import leshan.server.bootstrap.BootstrapStore;
import leshan.server.bootstrap.LwM2mBootstrapServer;
import leshan.server.security.SecurityStore;
import leshan.util.Validate;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.scandium.DTLSConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Lightweight M2M server, serving bootstrap information on /bs.
 */
public class LwM2mBootstrapServerImpl implements LwM2mBootstrapServer {

    private final static Logger LOG = LoggerFactory.getLogger(LwM2mBootstrapServerImpl.class);

    /** IANA assigned UDP port for CoAP (so for LWM2M) */
    public static final int PORT = 5683;

    /** IANA assigned UDP port for CoAP with DTLS (so for LWM2M) */
    public static final int PORT_DTLS = 5684;

    private final CoapServer coapServer;

    private final BootstrapStore bsStore;

    private final SecurityStore securityStore;

    public LwM2mBootstrapServerImpl(BootstrapStore bsStore, SecurityStore securityStore) {
        this(new InetSocketAddress((InetAddress) null, PORT), new InetSocketAddress((InetAddress) null, PORT_DTLS),
                bsStore, securityStore);

    }

    public LwM2mBootstrapServerImpl(InetSocketAddress localAddress, InetSocketAddress localAddressSecure,
            BootstrapStore bsStore, SecurityStore securityStore) {
        Validate.notNull(bsStore, "bootstrap store must not be null");

        this.bsStore = bsStore;
        this.securityStore = securityStore;
        // init CoAP server
        coapServer = new CoapServer();
        Endpoint endpoint = new CoAPEndpoint(localAddress);
        coapServer.addEndpoint(endpoint);

        // init DTLS server
        DTLSConnector connector = new DTLSConnector(localAddressSecure, null);
        connector.getConfig().setPskStore(new LwM2mPskStore(this.securityStore));

        Endpoint secureEndpoint = new SecureEndpoint(connector);
        coapServer.addEndpoint(secureEndpoint);

        // define /bs ressource
        BootstrapResource bsResource = new BootstrapResource(bsStore);
        coapServer.add(bsResource);
    }

    @Override
    public BootstrapStore getBoostrapStore() {
        return bsStore;
    }

    @Override
    public SecurityStore getSecurityStore() {
        return securityStore;
    }

    /**
     * Starts the server and binds it to the specified port.
     */
    public void start() {
        coapServer.start();
        LOG.info("LW-M2M server started");
    }

    /**
     * Stops the server and unbinds it from assigned ports (can be restarted).
     */
    public void stop() {
        coapServer.stop();
    }

    /**
     * Stops the server and unbinds it from assigned ports.
     */
    public void destroy() {
        coapServer.destroy();
    }
}