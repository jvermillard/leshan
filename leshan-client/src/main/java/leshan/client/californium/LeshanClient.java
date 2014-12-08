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
package leshan.client.californium;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import leshan.LinkObject;
import leshan.client.LwM2mClient;
import leshan.client.LwM2mServerMessageDeliverer;
import leshan.client.californium.impl.CaliforniumLwM2mClientRequestSender;
import leshan.client.coap.californium.CaliforniumBasedObject;
import leshan.client.request.LwM2mClientRequest;
import leshan.client.resource.LinkFormattable;
import leshan.client.resource.LwM2mClientObjectDefinition;
import leshan.client.response.OperationResponse;
import leshan.client.util.ResponseCallback;
import leshan.util.Validate;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.server.resources.Resource;

/**
 * A Lightweight M2M client.
 */
public class LeshanClient implements LwM2mClient {

    private final CoapServer clientSideServer;
    private final AtomicBoolean clientServerStarted = new AtomicBoolean(false);
    private final CaliforniumLwM2mClientRequestSender requestSender;

    public LeshanClient(final InetSocketAddress clientAddress, final InetSocketAddress serverAddress,
            final LwM2mClientObjectDefinition... objectDevice) {
        this(clientAddress, serverAddress, new CoapServer(), objectDevice);
    }

    public LeshanClient(final InetSocketAddress clientAddress, final InetSocketAddress serverAddress,
            final CoapServer serverLocal, final LwM2mClientObjectDefinition... objectDevice) {
        Validate.notNull(clientAddress);
        Validate.notNull(serverLocal);
        Validate.notNull(serverAddress);
        Validate.notNull(objectDevice);
        Validate.notEmpty(objectDevice);

        serverLocal.setMessageDeliverer(new LwM2mServerMessageDeliverer(serverLocal.getRoot()));
        final Endpoint endpoint = new CoAPEndpoint(clientAddress);
        serverLocal.addEndpoint(endpoint);

        clientSideServer = serverLocal;

        for (final LwM2mClientObjectDefinition def : objectDevice) {
            if (clientSideServer.getRoot().getChild(Integer.toString(def.getId())) != null) {
                throw new IllegalArgumentException("Trying to load Client Object of name '" + def.getId()
                        + "' when one was already added.");
            }

            final CaliforniumBasedObject clientObject = new CaliforniumBasedObject(def);

            clientSideServer.add(clientObject);
        }

        requestSender = new CaliforniumLwM2mClientRequestSender(serverLocal.getEndpoint(clientAddress), serverAddress,
                getObjectModel());
    }

    @Override
    public void start() {
        clientSideServer.start();
        clientServerStarted.set(true);
    }

    @Override
    public void stop() {
        clientSideServer.stop();
        clientServerStarted.set(false);
    }

    @Override
    public OperationResponse send(final LwM2mClientRequest request) {
        if (!clientServerStarted.get()) {
            return OperationResponse.failure(ResponseCode.INTERNAL_SERVER_ERROR,
                    "Leshan Client not started so unable to send request.");
        }
        return requestSender.send(request);
    }

    @Override
    public void send(final LwM2mClientRequest request, final ResponseCallback callback) {
        if (!clientServerStarted.get()) {
            callback.onFailure(OperationResponse.failure(ResponseCode.INTERNAL_SERVER_ERROR,
                    "Leshan Client not started so unable to send request."));
        } else {
            requestSender.send(request, callback);
        }
    }

    @Override
    public LinkObject[] getObjectModel(final Integer... ids) {
        if (ids.length > 3) {
            throw new IllegalArgumentException(
                    "An Object Model Only Goes 3 levels deep:  Object ID/ObjectInstance ID/Resource ID");
        }

        if (ids.length == 0) {
            final StringBuilder registrationMasterLinkObject = new StringBuilder();
            for (final Resource clientObject : clientSideServer.getRoot().getChildren()) {
                if (clientObject instanceof LinkFormattable) {
                    registrationMasterLinkObject.append(((LinkFormattable) clientObject).asLinkFormat()).append(",");
                }
            }

            registrationMasterLinkObject.deleteCharAt(registrationMasterLinkObject.length() - 1);

            return LinkObject.parse(registrationMasterLinkObject.toString().getBytes());
        }

        final Resource clientObject = clientSideServer.getRoot().getChild(Integer.toString(ids[0]));

        if (clientObject == null) {
            return new LinkObject[] {};
        } else if (ids.length == 1) {
            return LinkObject.parse(((LinkFormattable) clientObject).asLinkFormat().getBytes());
        }

        final Resource clientObjectInstance = clientObject.getChild(Integer.toString(ids[1]));

        if (clientObjectInstance == null) {
            return new LinkObject[] {};
        } else if (ids.length == 2) {
            return LinkObject.parse(((LinkFormattable) clientObjectInstance).asLinkFormat().getBytes());
        }

        final Resource clientResource = clientObjectInstance.getChild(Integer.toString(ids[2]));

        if (clientResource == null) {
            return new LinkObject[] {};
        }

        return LinkObject.parse(((LinkFormattable) clientResource).asLinkFormat().getBytes());
    }

}
