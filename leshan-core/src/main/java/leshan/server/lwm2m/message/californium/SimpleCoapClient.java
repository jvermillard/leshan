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
package leshan.server.lwm2m.message.californium;

import leshan.server.lwm2m.message.OperationType;
import leshan.server.lwm2m.message.RequestTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Endpoint;

public class SimpleCoapClient implements CoapClient {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleCoapClient.class);
    private static final int COAP_REQUEST_TIMEOUT_MILLIS = 5000;

    private final Endpoint endpoint;
    private final int timeoutMillis;

    public SimpleCoapClient(Endpoint endpoint) {
        this(endpoint, COAP_REQUEST_TIMEOUT_MILLIS);
    }

    public SimpleCoapClient(Endpoint endpoint, int requestTimeout) {
        if (endpoint == null) {
            throw new NullPointerException("Endpoint must not be null");
        }
        this.endpoint = endpoint;
        this.timeoutMillis = requestTimeout;
    }

    @Override
    public Response send(Request coapRequest, OperationType operationType) {
        this.endpoint.sendRequest(coapRequest);
        Response coapResponse = null;
        try {
            coapResponse = coapRequest.waitForResponse(this.timeoutMillis);
        } catch (InterruptedException e) {
            // no idea why some other thread should have interrupted this thread
            // but anyway, go ahead as if the timeout had been reached
            LOG.debug("Caught an unexpected InterruptedException during execution of CoAP request", e);
        }

        if (coapResponse == null) {
            throw new RequestTimeoutException(coapRequest.getURI(), this.timeoutMillis);
        } else {
            return coapResponse;
        }
    }

}
