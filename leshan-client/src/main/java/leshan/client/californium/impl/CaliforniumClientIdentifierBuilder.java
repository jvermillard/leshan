/*
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
package leshan.client.californium.impl;

import leshan.client.request.BootstrapRequest;
import leshan.client.request.DeregisterRequest;
import leshan.client.request.LwM2mClientRequestVisitor;
import leshan.client.request.LwM2mIdentifierRequest;
import leshan.client.request.RegisterRequest;
import leshan.client.request.UpdateRequest;
import leshan.client.request.identifier.ClientIdentifier;

import org.eclipse.californium.core.coap.Response;

public class CaliforniumClientIdentifierBuilder implements LwM2mClientRequestVisitor {

    private ClientIdentifier clientIdentifier;
    private final Response coapResponse;

    public CaliforniumClientIdentifierBuilder(final Response coapResponse) {
        this.coapResponse = coapResponse;
    }

    @Override
    public void visit(final RegisterRequest request) {
        buildClientIdentifier(request);
    }

    @Override
    public void visit(final DeregisterRequest request) {
        clientIdentifier = request.getClientIdentifier();
    }

    @Override
    public void visit(final UpdateRequest request) {
        clientIdentifier = request.getClientIdentifier();
    }

    @Override
    public void visit(final BootstrapRequest request) {
        buildClientIdentifier(request);
    }

    private void buildClientIdentifier(final LwM2mIdentifierRequest request) {
        clientIdentifier = new CaliforniumClientIdentifier(coapResponse.getOptions().getLocationString(),
                request.getClientEndpointIdentifier());
    }

    public ClientIdentifier getClientIdentifier() {
        return clientIdentifier;
    }

}
