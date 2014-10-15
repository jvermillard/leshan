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
package leshan.server.connector.californium;

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.impl.node.LwM2mNodeEncoder;
import leshan.server.lwm2m.node.LwM2mPath;
import leshan.server.lwm2m.request.CreateRequest;
import leshan.server.lwm2m.request.DeleteRequest;
import leshan.server.lwm2m.request.DiscoverRequest;
import leshan.server.lwm2m.request.ExecuteRequest;
import leshan.server.lwm2m.request.LwM2mRequestVisitor;
import leshan.server.lwm2m.request.ObserveRequest;
import leshan.server.lwm2m.request.ReadRequest;
import leshan.server.lwm2m.request.WriteAttributesRequest;
import leshan.server.lwm2m.request.WriteRequest;

import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;

public class CaliforniumCoapRequestBuilder implements LwM2mRequestVisitor {

    private Request coapRequest;

    @Override
    public void visit(ReadRequest request) {
        coapRequest = Request.newGet();
        setTarget(coapRequest, request.getClient(), request.getPath());
    }

    @Override
    public void visit(DiscoverRequest request) {
        coapRequest = Request.newGet();
        setTarget(coapRequest, request.getClient(), request.getPath());
        coapRequest.getOptions().setAccept(MediaTypeRegistry.APPLICATION_LINK_FORMAT);
    }

    @Override
    public void visit(WriteRequest request) {
        coapRequest = request.isReplaceRequest() ? Request.newPut() : Request.newPost();
        coapRequest.getOptions().setContentFormat(request.getContentFormat().getCode());
        coapRequest
                .setPayload(LwM2mNodeEncoder.encode(request.getNode(), request.getContentFormat(), request.getPath()));
        setTarget(coapRequest, request.getClient(), request.getPath());
    }

    @Override
    public void visit(WriteAttributesRequest request) {
        coapRequest = Request.newPut();
        setTarget(coapRequest, request.getClient(), request.getPath());
        for (String query : request.getObserveSpec().toQueryParams()) {
            coapRequest.getOptions().addURIQuery(query);
        }
    }

    @Override
    public void visit(ExecuteRequest request) {
        coapRequest = Request.newPost();
        setTarget(coapRequest, request.getClient(), request.getPath());
        coapRequest.setPayload(request.getParameters());
        if (request.getContentFormat() != null) {
            coapRequest.getOptions().setContentFormat(request.getContentFormat().getCode());
        }
    }

    @Override
    public void visit(CreateRequest request) {
        coapRequest = Request.newPost();
        coapRequest.getOptions().setContentFormat(request.getContentFormat().getCode());
        coapRequest.setPayload(LwM2mNodeEncoder.encode(request.getObjectInstance(), request.getContentFormat(),
                request.getPath()));
        setTarget(coapRequest, request.getClient(), request.getPath());
    }

    @Override
    public void visit(DeleteRequest request) {
        coapRequest = Request.newDelete();
        setTarget(coapRequest, request.getClient(), request.getPath());
    }

    @Override
    public void visit(ObserveRequest request) {
        coapRequest = Request.newGet();
        coapRequest.setObserve();
        setTarget(coapRequest, request.getClient(), request.getPath());
    }

    private final void setTarget(Request coapRequest, Client client, LwM2mPath path) {
        coapRequest.setDestination(client.getAddress());
        coapRequest.setDestinationPort(client.getPort());

        // objectId
        coapRequest.getOptions().addURIPath(Integer.toString(path.getObjectId()));

        // objectInstanceId
        if (path.getObjectInstanceId() == null) {
            if (path.getResourceId() != null) {
                coapRequest.getOptions().addURIPath("0"); // default instanceId
            }
        } else {
            coapRequest.getOptions().addURIPath(Integer.toString(path.getObjectInstanceId()));
        }

        // resourceId
        if (path.getResourceId() != null) {
            coapRequest.getOptions().addURIPath(Integer.toString(path.getResourceId()));
        }
    }

    public Request getRequest() {
        return coapRequest;
    };
}
