/*
 * Copyright (c) 2013, Sierra Wireless,
 * Copyright (c) 2014, Zebra Technologies,
 *
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
package org.eclipse.leshan.client.coap.californium;

import java.util.List;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.leshan.ObserveSpec;
import org.eclipse.leshan.client.exchange.LwM2mExchange;
import org.eclipse.leshan.client.response.CreateResponse;
import org.eclipse.leshan.client.response.LwM2mResponse;
import org.eclipse.leshan.client.util.ObserveSpecParser;

public class CaliforniumBasedLwM2mExchange implements LwM2mExchange {

    private final CoapExchange exchange;

    public CaliforniumBasedLwM2mExchange(final CoapExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public void respond(final LwM2mResponse response) {
        if (response instanceof CreateResponse) {
            final String objectId = getObjectId();
            exchange.setLocationPath(objectId + "/" + ((CreateResponse) response).getLocation());
        }

        exchange.respond(leshanToCalifornium(response.getCode()), response.getResponsePayload());
    }

    private ResponseCode leshanToCalifornium(final org.eclipse.leshan.ResponseCode code) {
        switch (code) {
        case BAD_REQUEST:
            return ResponseCode.BAD_REQUEST;
        case CHANGED:
            return ResponseCode.CHANGED;
        case CONTENT:
            return ResponseCode.CONTENT;
        case CREATED:
            return ResponseCode.CREATED;
        case DELETED:
            return ResponseCode.DELETED;
        case METHOD_NOT_ALLOWED:
            return ResponseCode.METHOD_NOT_ALLOWED;
        case NOT_FOUND:
            return ResponseCode.NOT_FOUND;
        case UNAUTHORIZED:
            return ResponseCode.UNAUTHORIZED;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public byte[] getRequestPayload() {
        return exchange.getRequestPayload();
    }

    private String getObjectId() {
        return getUriPaths().get(0);
    }

    @Override
    public boolean hasObjectInstanceId() {
        return getUriPaths().size() > 1;
    }

    @Override
    public int getObjectInstanceId() {
        final List<String> paths = getUriPaths();
        return paths.size() >= 2 ? Integer.parseInt(paths.get(1)) : 0;
    }

    private List<String> getUriPaths() {
        return exchange.getRequestOptions().getUriPath();
    }

    @Override
    public boolean isObserve() {
        return exchange.getRequestOptions().hasObserve() && exchange.getRequestCode() == CoAP.Code.GET;
    }

    @Override
    public ObserveSpec getObserveSpec() {
        if (exchange.advanced().getRequest().getOptions().getURIQueryCount() == 0) {
            return null;
        }
        final List<String> uriQueries = exchange.advanced().getRequest().getOptions().getUriQuery();
        return ObserveSpecParser.parse(uriQueries);
    }

}
