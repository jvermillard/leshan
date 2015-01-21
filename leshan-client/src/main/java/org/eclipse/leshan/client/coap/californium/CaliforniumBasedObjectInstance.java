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

import java.util.Map.Entry;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.leshan.client.resource.LinkFormattable;
import org.eclipse.leshan.client.resource.LwM2mClientObject;
import org.eclipse.leshan.client.resource.LwM2mClientObjectInstance;
import org.eclipse.leshan.client.resource.LwM2mClientResource;

public class CaliforniumBasedObjectInstance extends CaliforniumBasedLwM2mNode<LwM2mClientObjectInstance> implements
        LinkFormattable {

    public CaliforniumBasedObjectInstance(final int instanceId, final LwM2mClientObjectInstance instance) {
        super(instanceId, instance);
        for (final Entry<Integer, LwM2mClientResource> entry : instance.getAllResources().entrySet()) {
            final Integer resourceId = entry.getKey();
            final LwM2mClientResource resource = entry.getValue();
            add(new CaliforniumBasedResource(resourceId, resource));
        }
    }

    @Override
    public void handleDELETE(final CoapExchange exchange) {
        node.delete(new CaliforniumBasedLwM2mCallbackExchange<LwM2mClientObject>(exchange,
                new Callback<LwM2mClientObject>() {

                    @Override
                    public void onSuccess(LwM2mClientObject object) {
                        getParent().remove(CaliforniumBasedObjectInstance.this);
                    }

                    @Override
                    public void onFailure() {
                    }

                }));

        exchange.respond(ResponseCode.DELETED);
    }

    @Override
    public String asLinkFormat() {
        final StringBuilder linkFormat = LinkFormat.serializeResource(this).append(
                LinkFormat.serializeAttributes(getAttributes()));
        for (final Resource child : getChildren()) {
            linkFormat.append(LinkFormat.serializeResource(child));
        }
        linkFormat.deleteCharAt(linkFormat.length() - 1);

        return linkFormat.toString();
    }

}