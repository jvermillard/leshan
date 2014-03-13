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

import leshan.server.LwM2mServer;
import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.client.RequestHandler;
import leshan.server.lwm2m.resource.RegisterResource;

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
public class CoapServer {

    private ch.ethz.inf.vs.californium.server.Server coapServer;

    private static final Logger LOG = LoggerFactory.getLogger(LwM2mServer.class);

    /** IANA assigned UDP port for CoAP (so for LWM2M) */
    public static final int PORT = 5684;

    private final RequestHandler requestHandler;

    public CoapServer(ClientRegistry clientRegistry) {
        // init CoAP server
        coapServer = new ch.ethz.inf.vs.californium.server.Server(PORT);

        // define /rd resource
        RegisterResource rdResource = new RegisterResource(clientRegistry);
        coapServer.add(rdResource);

        this.requestHandler = new RequestHandler(coapServer.getEndpoints().get(0));
    }

    /**
     * Starts the server and binds it to assigned UDP port for LW-M2M (5684).
     */
    public void start() {
        coapServer.start();
        LOG.info("LW-M2M server started on port " + PORT);
    }

    /**
     * Stops the server and unbinds it from assigned port.
     */
    public void stop() {
        coapServer.stop();
    }

    public RequestHandler getRequestHandler() {
        return this.requestHandler;
    }
}
