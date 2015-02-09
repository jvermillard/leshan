/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Zebra Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.client.coap.californium;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.leshan.client.resource.LwM2mClientObject;
import org.eclipse.leshan.client.resource.LwM2mClientObjectDefinition;
import org.eclipse.leshan.client.resource.LwM2mClientObjectInstance;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.node.codec.InvalidValueException;
import org.eclipse.leshan.core.node.codec.LwM2mNodeDecoder;
import org.eclipse.leshan.core.request.ContentFormat;
import org.eclipse.leshan.core.response.CreateResponse;

public class CaliforniumBasedObject extends CaliforniumBasedLwM2mNode<LwM2mClientObject> {

    public CaliforniumBasedObject(final LwM2mClientObjectDefinition def) {
        super(def.getId(), new LwM2mClientObject(def));

        if (def.isMandatory()) {
            createMandatoryObjectInstance(def);
        }
    }

    private void createMandatoryObjectInstance(final LwM2mClientObjectDefinition def) {
        LwM2mClientObjectInstance instance = node.createMandatoryInstance();
        add(new CaliforniumBasedObjectInstance(instance.getId(), instance));
    }

    @Override
    public void handlePOST(final CoapExchange coapExchange) {
        ContentFormat contentFormat = ContentFormat.fromCode(coapExchange.getRequestOptions().getContentFormat());
        LwM2mNode lwM2mNode;
        try {
            String instancePath = getFullPath() + "/" + getNewInstanceId();
            LwM2mPath lwM2mPath = new LwM2mPath(instancePath);
            lwM2mNode = LwM2mNodeDecoder.decode(coapExchange.getRequestPayload(), contentFormat, lwM2mPath);
            CreateResponse response = node.createInstance(lwM2mNode);
            if (response.getCode() == org.eclipse.leshan.ResponseCode.CREATED) {
                LwM2mClientObjectInstance instance = node.getInstance(lwM2mPath.getObjectInstanceId());
                add(new CaliforniumBasedObjectInstance(instance.getId(), instance));
                coapExchange.respond(fromLwM2mCode(response.getCode()), lwM2mPath.toString());
            } else {
                coapExchange.respond(fromLwM2mCode(response.getCode()));
            }
        } catch (InvalidValueException e) {
            coapExchange.respond(ResponseCode.INTERNAL_SERVER_ERROR);
        }

    }

    private int getNewInstanceId() {
        // TODO should be thread safe ?
        int i = 0;
        while (this.getChild(Integer.toString(i)) != null) {
            i++;
        }
        return i;
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
