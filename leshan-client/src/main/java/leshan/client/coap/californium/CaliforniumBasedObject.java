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

import leshan.client.resource.LwM2mClientObject;
import leshan.client.resource.LwM2mClientObjectDefinition;
import leshan.client.resource.LwM2mClientObjectInstance;

import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

public class CaliforniumBasedObject extends CaliforniumBasedLwM2mNode<LwM2mClientObject> {

    public CaliforniumBasedObject(final LwM2mClientObjectDefinition def) {
        super(def.getId(), new LwM2mClientObject(def));

        if (def.isMandatory()) {
            createMandatoryObjectInstance(def);
        }
    }

    private void createMandatoryObjectInstance(final LwM2mClientObjectDefinition def) {
        LwM2mClientObjectInstance instance = node.createMandatoryInstance();
        onSuccessfulCreate(instance);
    }

    @Override
    public void handlePOST(final CoapExchange exchange) {
        node.createInstance(new CaliforniumBasedLwM2mCallbackExchange<LwM2mClientObjectInstance>(exchange,
                getCreateCallback()));
    }

    private Callback<LwM2mClientObjectInstance> getCreateCallback() {
        return new Callback<LwM2mClientObjectInstance>() {

            @Override
            public void onSuccess(final LwM2mClientObjectInstance newInstance) {
                onSuccessfulCreate(newInstance);
            }

            @Override
            public void onFailure() {
            }

        };
    }

    public void onSuccessfulCreate(final LwM2mClientObjectInstance instance) {
        add(new CaliforniumBasedObjectInstance(instance.getId(), instance));
        node.onSuccessfulCreate(instance);
    }

    @Override
    public String asLinkFormat() {
        final StringBuilder linkFormat = LinkFormat.serializeResource(this).append(
                LinkFormat.serializeAttributes(getAttributes()));
        for (final Resource child : getChildren()) {
            for (final Resource grandchild : child.getChildren()) {
                linkFormat.append(LinkFormat.serializeResource(grandchild));
            }
        }
        linkFormat.deleteCharAt(linkFormat.length() - 1);
        return linkFormat.toString();
    }

}
