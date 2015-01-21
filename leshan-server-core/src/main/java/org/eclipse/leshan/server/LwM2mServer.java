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
package org.eclipse.leshan.server;

import org.eclipse.leshan.core.request.DownlinkRequest;
import org.eclipse.leshan.core.response.ExceptionConsumer;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ResponseConsumer;
import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.server.client.ClientRegistry;
import org.eclipse.leshan.server.observation.ObservationRegistry;
import org.eclipse.leshan.server.security.SecurityRegistry;

/**
 * An OMA Lightweight M2M device management server.
 *
 * Will receive client registration through the "/rd" resource. Is able to send requests (Read, Write, Create, Delete,
 * Execute, Discover, Observer) to specified clients.
 *
 * It's your main entry point for using the Leshan-core API.
 */
public interface LwM2mServer {

    /**
     * Start the server (bind port, start to listen CoAP messages.
     */
    void start();

    /**
     * Stops the server, i.e. unbinds it from all ports. Frees as much system resources as possible to still be able to
     * be started.
     */
    void stop();

    /**
     * Destroys the server, i.e. unbinds from all ports and frees all system resources.
     */
    void destroy();

    /**
     * Send a Lightweight M2M request synchronously. Will block until a response is received from the remote client.
     */
    <T extends LwM2mResponse> T send(Client destination, DownlinkRequest<T> request);

    /**
     * Send a Lightweight M2M request asynchronously.
     */
    <T extends LwM2mResponse> void send(Client destination, DownlinkRequest<T> request,
            ResponseConsumer<T> responseCallback, ExceptionConsumer errorCallback);

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
