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

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.leshan.ObserveSpec;
import org.eclipse.leshan.client.resource.LinkFormattable;
import org.eclipse.leshan.client.resource.LwM2mClientNode;
import org.eclipse.leshan.client.resource.NotifySender;
import org.eclipse.leshan.client.util.ObserveSpecParser;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.Value.DataType;
import org.eclipse.leshan.core.node.codec.InvalidValueException;
import org.eclipse.leshan.core.node.codec.LwM2mNodeDecoder;
import org.eclipse.leshan.core.node.codec.LwM2mNodeEncoder;
import org.eclipse.leshan.core.objectspec.ResourceSpec;
import org.eclipse.leshan.core.objectspec.ResourceSpec.Type;
import org.eclipse.leshan.core.objectspec.Resources;
import org.eclipse.leshan.core.request.ContentFormat;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ValueResponse;
import org.eclipse.leshan.util.Validate;

public abstract class CaliforniumBasedLwM2mNode<T extends LwM2mClientNode> extends CoapResource implements
        LinkFormattable, NotifySender {

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
            coapExchange.respond(ResponseCode.CONTENT, asLinkFormat(), MediaTypeRegistry.APPLICATION_LINK_FORMAT);
        } else {
            if (coapExchange.getRequestOptions().hasObserve()) {
                node.observe(this, service);
            }

            // manage read request
            ValueResponse response = node.read();
            if (response.getCode() == org.eclipse.leshan.ResponseCode.CONTENT) {
                LwM2mPath path = new LwM2mPath(getFullPath());
                LwM2mNode content = response.getContent();
                ContentFormat format;
                // TODO this code is duplicate from write request and should be reused.
                // guess content type
                // Manage default format
                // Use text for single resource ...
                if (path.isResource()) {
                    // Use resource description to guess
                    final ResourceSpec description = Resources
                            .getResourceSpec(path.getObjectId(), path.getResourceId());
                    if (description != null) {
                        if (description.multiple) {
                            format = ContentFormat.TLV;
                        } else {
                            format = description.type == Type.OPAQUE ? ContentFormat.OPAQUE : ContentFormat.TEXT;
                        }
                    }
                    // If no object description available, use 'node' to guess
                    else {
                        LwM2mResource resourceNode = ((LwM2mResource) content);
                        if (resourceNode.isMultiInstances()) {
                            format = ContentFormat.TLV;
                        } else {
                            format = resourceNode.getValue().type == DataType.OPAQUE ? ContentFormat.OPAQUE
                                    : ContentFormat.TEXT;
                        }
                    }
                }
                // ... and TLV for other ones.
                else {
                    format = ContentFormat.TLV;
                }
                coapExchange.respond(ResponseCode.CONTENT, LwM2mNodeEncoder.encode(content, format, path));
            } else {
                coapExchange.respond(fromLwM2mCode(response.getCode()));
            }
        }
    }

    @Override
    public void handlePUT(final CoapExchange coapExchange) {
        // get observeSpec
        ObserveSpec spec = null;
        if (coapExchange.advanced().getRequest().getOptions().getURIQueryCount() != 0) {
            final List<String> uriQueries = coapExchange.advanced().getRequest().getOptions().getUriQuery();
            spec = ObserveSpecParser.parse(uriQueries);
        }

        if (spec != null) {
            node.writeAttributes(spec);
        } else {
            ContentFormat contentFormat = ContentFormat.fromCode(coapExchange.getRequestOptions().getContentFormat());
            LwM2mNode lwM2mNode;
            try {
                lwM2mNode = LwM2mNodeDecoder.decode(coapExchange.getRequestPayload(), contentFormat, new LwM2mPath(
                        getFullPath()));
                LwM2mResponse response = node.write(lwM2mNode);
                coapExchange.respond(fromLwM2mCode(response.getCode()));
            } catch (InvalidValueException e) {
                coapExchange.respond(ResponseCode.INTERNAL_SERVER_ERROR);
            }

        }
    }

    public String getFullPath() {
        return getPath() + getName();
    }

    @Override
    public void sendNotify() {
        this.changed();
    }

    public static ResponseCode fromLwM2mCode(final org.eclipse.leshan.ResponseCode code) {
        Validate.notNull(code);

        switch (code) {
        case CREATED:
            return ResponseCode.CREATED;
        case DELETED:
            return ResponseCode.DELETED;
        case CHANGED:
            return ResponseCode.CHANGED;
        case CONTENT:
            return ResponseCode.CONTENT;
        case BAD_REQUEST:
            return ResponseCode.BAD_REQUEST;
        case UNAUTHORIZED:
            return ResponseCode.UNAUTHORIZED;
        case NOT_FOUND:
            return ResponseCode.NOT_FOUND;
        case METHOD_NOT_ALLOWED:
            return ResponseCode.METHOD_NOT_ALLOWED;
        default:
            // TODO how can we manage CONFLICT code ...
            // } else if (code == leshan.ResponseCode.CONFLICT) {
            // //return 137;
            // } else {
            throw new IllegalArgumentException("Invalid CoAP code for LWM2M response: " + code);
        }
    }
}
