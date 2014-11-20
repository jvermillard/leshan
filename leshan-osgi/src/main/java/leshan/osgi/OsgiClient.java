/*
 * Copyright (c) 2014, Bosch Software Innovations GmbH
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
package leshan.osgi;

import java.io.UnsupportedEncodingException;

import leshan.LinkObject;
import leshan.core.response.ClientResponse;
import leshan.core.response.DiscoverResponse;
import leshan.core.response.ValueResponse;
import leshan.server.client.Client;
import leshan.server.request.DiscoverRequest;
import leshan.server.request.ExecuteRequest;
import leshan.server.request.ObserveRequest;
import leshan.server.request.ReadRequest;
import leshan.server.request.WriteAttributesRequest;
import leshan.server.request.WriteRequest;

/**
 * The OsgiClient Interface includes the methods of communication lwm2m.
 */
public interface OsgiClient {

    /**
     * Send a ReadRequest to the client.
     *
     * @param readRequest
     * @return the ValueResponse or <code>null</code> if timeout occurred.
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     */
    ValueResponse read(ReadRequest readRequest) throws InterruptedException, UnsupportedEncodingException;

    /**
     * Send a ExecuteRequest to the client.
     *
     * @param executeReqest
     * @return the ClientResponse or <code>null</code> if timeout occurred.
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     */
    ClientResponse execute(ExecuteRequest executeReqest);

    /**
     * Send a WriteRequest to the client.
     *
     * @param writeRequest
     * @return the ClientResponse or <code>null</code> if timeout occurred.
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     */
    ClientResponse write(WriteRequest writeRequest);

    /**
     * Send a WriteAttributesRequest to the client.
     *
     * @param writeRequest
     * @return the ClientResponse or <code>null</code> if timeout occurred.
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     */
    ClientResponse writeAttribute(WriteAttributesRequest writeRequest);

    /**
     * Send a ObserveRequest to the client.
     *
     * @param observeRequest
     * @return the ValueResponse or <code>null</code> if timeout occurred.
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     */
    ValueResponse observe(ObserveRequest observeRequest);

    /**
     * Send a DiscoverRequest to the client.
     *
     * @param discoverRequest
     * @return the DiscoverResponse or <code>null</code> if timeout occurred.
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     */
    DiscoverResponse discover(DiscoverRequest discoverRequest);

    /**
     * return a {@link LinkObject}[] Array from the client.
     *
     * @return LinkObject[]
     */
    LinkObject[] getObjectLinks();

    /**
     * Returns the Client.
     *
     * @return {@link Client}
     */
    Client getClient();

}
