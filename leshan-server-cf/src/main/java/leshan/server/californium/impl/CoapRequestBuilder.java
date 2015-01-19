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
package leshan.server.californium.impl;

import leshan.core.node.LwM2mPath;
import leshan.core.node.codec.LwM2mNodeEncoder;
import leshan.core.request.CreateRequest;
import leshan.core.request.DeleteRequest;
import leshan.core.request.DiscoverRequest;
import leshan.core.request.ExecuteRequest;
import leshan.core.request.DownlinkRequestVisitor;
import leshan.core.request.ObserveRequest;
import leshan.core.request.ReadRequest;
import leshan.core.request.WriteAttributesRequest;
import leshan.core.request.WriteRequest;
import leshan.server.client.Client;
import leshan.util.StringUtils;

import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;

public class CoapRequestBuilder implements DownlinkRequestVisitor {

    private Request coapRequest;
    private final Client destination;

    public CoapRequestBuilder(Client destination) {
        this.destination = destination;
    }

    @Override
    public void visit(ReadRequest request) {
        coapRequest = Request.newGet();
        setTarget(coapRequest, destination, request.getPath());
    }

    @Override
    public void visit(DiscoverRequest request) {
        coapRequest = Request.newGet();
        setTarget(coapRequest, destination, request.getPath());
        coapRequest.getOptions().setAccept(MediaTypeRegistry.APPLICATION_LINK_FORMAT);
    }

    @Override
    public void visit(WriteRequest request) {
        coapRequest = request.isReplaceRequest() ? Request.newPut() : Request.newPost();
        coapRequest.getOptions().setContentFormat(request.getContentFormat().getCode());
        coapRequest
                .setPayload(LwM2mNodeEncoder.encode(request.getNode(), request.getContentFormat(), request.getPath()));
        setTarget(coapRequest, destination, request.getPath());
    }

    @Override
    public void visit(WriteAttributesRequest request) {
        coapRequest = Request.newPut();
        setTarget(coapRequest, destination, request.getPath());
        for (String query : request.getObserveSpec().toQueryParams()) {
            coapRequest.getOptions().addUriQuery(query);
        }
    }

    @Override
    public void visit(ExecuteRequest request) {
        coapRequest = Request.newPost();
        setTarget(coapRequest, destination, request.getPath());
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
        setTarget(coapRequest, destination, request.getPath());
    }

    @Override
    public void visit(DeleteRequest request) {
        coapRequest = Request.newDelete();
        setTarget(coapRequest, destination, request.getPath());
    }

    @Override
    public void visit(ObserveRequest request) {
        coapRequest = Request.newGet();
        coapRequest.setObserve();
        setTarget(coapRequest, destination, request.getPath());
    }

    private final void setTarget(Request coapRequest, Client client, LwM2mPath path) {
        coapRequest.setDestination(client.getAddress());
        coapRequest.setDestinationPort(client.getPort());

        // root path
        if (client.getRootPath() != null) {
            for (String rootPath : client.getRootPath().split("/")) {
                if (!StringUtils.isEmpty(rootPath)) {
                    coapRequest.getOptions().addUriPath(rootPath);
                }
            }
        }

        // objectId
        coapRequest.getOptions().addUriPath(Integer.toString(path.getObjectId()));

        // objectInstanceId
        if (path.getObjectInstanceId() == null) {
            if (path.getResourceId() != null) {
                coapRequest.getOptions().addUriPath("0"); // default instanceId
            }
        } else {
            coapRequest.getOptions().addUriPath(Integer.toString(path.getObjectInstanceId()));
        }

        // resourceId
        if (path.getResourceId() != null) {
            coapRequest.getOptions().addUriPath(Integer.toString(path.getResourceId()));
        }
    }

    public Request getRequest() {
        return coapRequest;
    };
}
