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
package leshan.client.lwm2m;

import java.net.InetSocketAddress;

import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.client.lwm2m.bootstrap.BootstrapUplink;
import leshan.client.lwm2m.coap.californium.CaliforniumBasedObject;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.resource.LinkFormattable;
import leshan.client.lwm2m.resource.LwM2mClientObjectDefinition;
import leshan.server.lwm2m.client.LinkObject;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.EmptyMessage;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.interceptors.MessageInterceptor;
import org.eclipse.californium.core.server.resources.Resource;

/**
 * A Lightweight M2M client.
 */
public class LwM2mClient {

    private final CoapServer clientSideServer;

    public LwM2mClient(final LwM2mClientObjectDefinition... defs) {
        this(new CoapServer(), defs);
    }

    public LwM2mClient(final CoapServer server, final LwM2mClientObjectDefinition... defs) {
        if (defs == null || defs.length == 0) {
            throw new IllegalArgumentException(
                    "LWM2M Clients must support minimum required Objects defined in the LWM2M Specification.");
        }
        server.setMessageDeliverer(new LwM2mServerMessageDeliverer(server.getRoot()));
        clientSideServer = server;

        for (final LwM2mClientObjectDefinition def : defs) {
            if (clientSideServer.getRoot().getChild(Integer.toString(def.getId())) != null) {
                throw new IllegalArgumentException("Trying to load Client Object of name '" + def.getId()
                        + "' when one was already added.");
            }

            final CaliforniumBasedObject clientObject = new CaliforniumBasedObject(def);

            clientSideServer.add(clientObject);
        }
    }

    public void start() {
        clientSideServer.start();
    }

    public void stop() {
        clientSideServer.stop();
    }

    public BootstrapUplink startBootstrap(final int port, final InetSocketAddress destination,
            final BootstrapDownlink downlink) {
        final BootstrapUplink uplink = new BootstrapUplink(destination, new CoAPEndpoint(port), downlink);

        return uplink;
    }

    public RegisterUplink startRegistration(final int port, final InetSocketAddress destination) {
        CoAPEndpoint endpoint = (CoAPEndpoint) clientSideServer.getEndpoint(port);
        if (endpoint == null) {
            endpoint = new CoAPEndpoint(port);
        }

        clientSideServer.addEndpoint(endpoint);
        clientSideServer.start();

        return new RegisterUplink(destination, endpoint, this);
    }

    public RegisterUplink startRegistration(final InetSocketAddress local, final InetSocketAddress destination) {
        CoAPEndpoint endpoint = (CoAPEndpoint) clientSideServer.getEndpoint(local);
        if (endpoint == null) {
            endpoint = new CoAPEndpoint(local);
        }
        System.out.println("Endpoint: " + endpoint);
        System.out.println("Destination: " + destination);

        // TODO: EDGEBOX-3507 Andrew Summers 9/29/14
        // This shouldn't be necessary. Figure out if we
        // need to do this further down the stack
        endpoint.addInterceptor(new MessageInterceptor() {

            @Override
            public void sendResponse(final Response response) {
                // TODO Auto-generated method stub

            }

            @Override
            public void sendRequest(final Request request) {
                request.setDestination(destination.getAddress());
                request.setDestinationPort(destination.getPort());
                request.setSource(local.getAddress());
                request.setSourcePort(local.getPort());
                System.out.println("Sending request to: " + request.getDestination() + " from " + request.getSource());
            }

            @Override
            public void sendEmptyMessage(final EmptyMessage message) {
                // TODO Auto-generated method stub

            }

            @Override
            public void receiveResponse(final Response response) {
                System.out.println("Received response: " + response.getCode());
            }

            @Override
            public void receiveRequest(final Request request) {
                // TODO Auto-generated method stub

            }

            @Override
            public void receiveEmptyMessage(final EmptyMessage message) {
                // TODO Auto-generated method stub

            }
        });

        clientSideServer.addEndpoint(endpoint);
        clientSideServer.start();

        return new RegisterUplink(destination, endpoint, this);
    }

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
