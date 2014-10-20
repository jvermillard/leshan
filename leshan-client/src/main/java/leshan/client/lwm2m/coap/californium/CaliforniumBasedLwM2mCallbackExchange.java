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
package leshan.client.lwm2m.coap.californium;

import leshan.client.lwm2m.exchange.LwM2mCallbackExchange;
import leshan.client.lwm2m.resource.LwM2mClientNode;
import leshan.client.lwm2m.response.LwM2mResponse;

import org.eclipse.californium.core.server.resources.CoapExchange;

public class CaliforniumBasedLwM2mCallbackExchange<T extends LwM2mClientNode> extends CaliforniumBasedLwM2mExchange
        implements LwM2mCallbackExchange<T> {

    private final Callback<T> callback;
    private T node;

    public CaliforniumBasedLwM2mCallbackExchange(final CoapExchange exchange, final Callback<T> callback) {
        super(exchange);
        this.callback = callback;
    }

    @Override
    public void respond(final LwM2mResponse response) {
        if (response.isSuccess()) {
            callback.onSuccess(node);
        } else {
            callback.onFailure();
        }
        super.respond(response);
    }

    @Override
    public void setNode(final T node) {
        this.node = node;
    }

}
