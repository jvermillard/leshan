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
package leshan.client.coap.californium;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import leshan.ObserveSpec;
import leshan.client.exchange.LwM2mExchange;
import leshan.client.resource.LinkFormattable;
import leshan.client.resource.LwM2mClientNode;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;

public abstract class CaliforniumBasedLwM2mNode<T extends LwM2mClientNode> extends CoapResource implements
        LinkFormattable {

    private static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    protected T node;

    public CaliforniumBasedLwM2mNode(int id, T node) {
        super(Integer.toString(id));
        setObservable(true);
        this.node = node;
    }
    
    public T getLwM2mClientObject() {
        return node;
    }

    @Override
    public void handleGET(final CoapExchange coapExchange) {
        if (coapExchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_LINK_FORMAT) {
            handleDiscover(coapExchange);
        } else {
            LwM2mExchange exchange = new CaliforniumBasedLwM2mExchange(coapExchange);
            if (exchange.isObserve()) {
                node.observe(exchange, service);
            }
            node.read(exchange);
        }
    }

    @Override
    public void handlePUT(final CoapExchange coapExchange) {
        LwM2mExchange exchange = new CaliforniumBasedLwM2mExchange(coapExchange);
        final ObserveSpec spec = exchange.getObserveSpec();
        if (spec != null) {
            node.writeAttributes(exchange, spec);
        } else {
            node.write(exchange);
        }
    }

    protected void handleDiscover(final CoapExchange exchange) {
        exchange.respond(ResponseCode.CONTENT, asLinkFormat(), MediaTypeRegistry.APPLICATION_LINK_FORMAT);
    }

}
