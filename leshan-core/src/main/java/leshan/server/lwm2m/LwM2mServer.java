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

import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.request.ClientResponse;
import leshan.server.lwm2m.request.LwM2mRequest;
import leshan.server.lwm2m.request.ResponseCallback;
import leshan.server.lwm2m.security.SecurityRegistry;

/**
 * An OMA Lightweight M2M device management server.
 * 
 * Will receive client registration through the "/rd" resource.
 * Is able to send requests (Read, Write, Create, Delete, Execute, Discover, Observer) to specified clients.
 * 
 * It's your main entry point for using the Leshan-core API.
 */
public interface LwM2mServer {

    /**
     * Start the server (bind port, start to listen CoAP messages.
     */
    void start();

    /**
     * Stop the server, release the resources (like UDP ports). 
     */
    void stop();

    /**
     * Send a Lightweight M2M request synchronously. Will block until a response is received from the remote client.
     */
    <T extends ClientResponse> T send(LwM2mRequest<T> request);

    /**
     * Send a Lightweight M2M request asynchronously.
     */
    <T extends ClientResponse> void send(LwM2mRequest<T> request, ResponseCallback<T> callback);

    /**
     * Get the client registry containing the list of connected clients. You can use this object for listening client
     * registration/deregistration.
     */
    ClientRegistry getClientRegistry();

    /**
     * Get the Observation registry containing of current observation. You can use this object for listening resource
     * observation or cancel it.
     */
    ObservationRegistry getObservationRegistry();

    /**
     * Get the SecurityRegistry containing of security information.
     */
    SecurityRegistry getSecurityRegistry();
}
