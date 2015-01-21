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
package org.eclipse.leshan.server.californium.impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MessageObserverAdapter;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.node.codec.InvalidValueException;
import org.eclipse.leshan.core.node.codec.LwM2mNodeDecoder;
import org.eclipse.leshan.core.request.ContentFormat;
import org.eclipse.leshan.core.response.ValueResponse;
import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.server.observation.Observation;
import org.eclipse.leshan.server.observation.ObservationListener;
import org.eclipse.leshan.util.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CaliforniumObservation extends MessageObserverAdapter implements Observation {
    private final Logger LOG = LoggerFactory.getLogger(CaliforniumObservation.class);

    private final Request coapRequest;
    private final List<ObservationListener> listeners = new CopyOnWriteArrayList<>();
    private final Client client;
    private final LwM2mPath path;

    public CaliforniumObservation(Request coapRequest, Client client, LwM2mPath path) {
        Validate.notNull(coapRequest);
        Validate.notNull(client);
        Validate.notNull(path);

        this.coapRequest = coapRequest;
        this.client = client;
        this.path = path;
    }

    public CaliforniumObservation(Request coapRequest, Client client, LwM2mPath path, ObservationListener listener) {
        this(coapRequest, client, path);
        this.listeners.add(listener);
    }

    public CaliforniumObservation(Request coapRequest, Client client, LwM2mPath path,
            List<ObservationListener> listeners) {
        this(coapRequest, client, path);
        this.listeners.addAll(listeners);
    }

    @Override
    public void cancel() {
        coapRequest.cancel();
    }

    @Override
    public void onResponse(Response coapResponse) {
        if (coapResponse.getCode() == CoAP.ResponseCode.CHANGED) {
            try {
                LwM2mNode content = LwM2mNodeDecoder.decode(coapResponse.getPayload(),
                        ContentFormat.fromCode(coapResponse.getOptions().getContentFormat()), path);
                ValueResponse response = new ValueResponse(ResponseCode.CHANGED, content);

                for (ObservationListener listener : listeners) {
                    listener.newValue(this, response.getContent());
                }
            } catch (InvalidValueException e) {
                String msg = String.format("[%s] ([%s])", e.getMessage(), e.getPath().toString());
                LOG.debug(msg);
            }
        }
    }

    @Override
    public void onCancel() {
        for (ObservationListener listener : listeners) {
            listener.cancelled(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client getClient() {
        return client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LwM2mPath getPath() {
        return path;
    }

    @Override
    public String toString() {
        return String.format("CaliforniumObservation [%s]", path);
    }

    @Override
    public void addListener(ObservationListener listener) {
        listeners.add(listener);

    }

    @Override
    public void removeListener(ObservationListener listener) {
        listeners.remove(listener);

    }
}
