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
package org.eclipse.leshan.client.californium.impl;

import java.util.logging.Logger;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.leshan.client.request.BootstrapRequest;
import org.eclipse.leshan.client.request.DeregisterRequest;
import org.eclipse.leshan.client.request.LwM2mClientRequest;
import org.eclipse.leshan.client.request.LwM2mClientRequestVisitor;
import org.eclipse.leshan.client.request.RegisterRequest;
import org.eclipse.leshan.client.request.UpdateRequest;
import org.eclipse.leshan.client.response.OperationResponse;

public class LwM2mClientResponseBuilder implements LwM2mClientRequestVisitor {
    private static final Logger LOG = Logger.getLogger(LwM2mClientResponseBuilder.class.getCanonicalName());

    private final Request coapRequest;
    private final Response coapResponse;
    private final CaliforniumLwM2mClientRequestSender californiumLwM2mClientRequestSender;
    private OperationResponse lwM2mresponse;
    private final CaliforniumClientIdentifierBuilder californiumClientIdentifierBuilder;

    public LwM2mClientResponseBuilder(final Request coapRequest, final Response coapResponse,
            final CaliforniumLwM2mClientRequestSender californiumLwM2mClientRequestSender) {
        this.coapRequest = coapRequest;
        this.coapResponse = coapResponse;
        this.californiumLwM2mClientRequestSender = californiumLwM2mClientRequestSender;
        this.californiumClientIdentifierBuilder = new CaliforniumClientIdentifierBuilder(coapResponse);
    }

    @Override
    public void visit(final RegisterRequest request) {
        buildClientIdentifier(request);
        buildResponse();
    }

    @Override
    public void visit(final DeregisterRequest request) {
        buildClientIdentifier(request);
        buildResponse();
    }

    @Override
    public void visit(final UpdateRequest request) {
        buildClientIdentifier(request);
        buildResponse();
    }

    @Override
    public void visit(final BootstrapRequest request) {
        buildClientIdentifier(request);
        buildResponse();
    }

    private void buildClientIdentifier(final LwM2mClientRequest request) {
        request.accept(californiumClientIdentifierBuilder);
    }

    private void buildResponse() {
        if (coapResponse == null) {
            lwM2mresponse = OperationResponse.failure(ResponseCode.GATEWAY_TIMEOUT, "Timed Out Waiting For Response.");
        } else if (ResponseCode.isSuccess(coapResponse.getCode())) {
            lwM2mresponse = OperationResponse
                    .of(coapResponse, californiumClientIdentifierBuilder.getClientIdentifier());
        } else {
            lwM2mresponse = OperationResponse.failure(coapResponse.getCode(), "Request Failed on Server "
                    + coapResponse.getOptions());
        }
    }

    public OperationResponse getResponse() {
        return lwM2mresponse;
    }

}
