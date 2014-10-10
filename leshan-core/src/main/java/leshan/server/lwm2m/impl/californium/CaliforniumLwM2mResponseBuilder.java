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
package leshan.server.lwm2m.impl.californium;

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.LinkObject;
import leshan.server.lwm2m.impl.node.InvalidValueException;
import leshan.server.lwm2m.impl.node.LwM2mNodeDecoder;
import leshan.server.lwm2m.node.LwM2mNode;
import leshan.server.lwm2m.node.LwM2mPath;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.request.ClientResponse;
import leshan.server.lwm2m.request.ContentFormat;
import leshan.server.lwm2m.request.CreateRequest;
import leshan.server.lwm2m.request.DeleteRequest;
import leshan.server.lwm2m.request.DiscoverRequest;
import leshan.server.lwm2m.request.DiscoverResponse;
import leshan.server.lwm2m.request.ExecuteRequest;
import leshan.server.lwm2m.request.LwM2mRequestVisitor;
import leshan.server.lwm2m.request.ObserveRequest;
import leshan.server.lwm2m.request.ReadRequest;
import leshan.server.lwm2m.request.ResourceAccessException;
import leshan.server.lwm2m.request.ResponseCode;
import leshan.server.lwm2m.request.ValueResponse;
import leshan.server.lwm2m.request.WriteAttributesRequest;
import leshan.server.lwm2m.request.WriteRequest;

import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaliforniumLwM2mResponseBuilder<T extends ClientResponse> implements LwM2mRequestVisitor {

    private static final Logger LOG = LoggerFactory.getLogger(CaliforniumLwM2mResponseBuilder.class);

    private ClientResponse lwM2mresponse;
    private Request coapRequest;
    private Response coapResponse;
    private ObservationRegistry observationRegistry;

    public CaliforniumLwM2mResponseBuilder(Request coapRequest, Response coapResponse,
            ObservationRegistry observationRegistry) {
        super();
        this.coapRequest = coapRequest;
        this.coapResponse = coapResponse;
        this.observationRegistry = observationRegistry;
    }

    @Override
    public void visit(ReadRequest request) {
        switch (coapResponse.getCode()) {
        case CONTENT:
            lwM2mresponse = buildContentResponse(request.getPath(), coapResponse);
            break;
        case UNAUTHORIZED:
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            lwM2mresponse = new ValueResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
            break;
        default:
            handleUnexpectedResponseCode(request.getClient(), coapRequest, coapResponse);
        }
    }

    @Override
    public void visit(DiscoverRequest request) {
        switch (coapResponse.getCode()) {
        case CONTENT:
            LinkObject[] links = null;
            if (MediaTypeRegistry.APPLICATION_LINK_FORMAT != coapResponse.getOptions().getContentFormat()) {
                LOG.debug("Expected LWM2M Client [{}] to return application/link-format [{}] content but got [{}]",
                        request.getClient().getEndpoint(), MediaTypeRegistry.APPLICATION_LINK_FORMAT, coapResponse
                                .getOptions().getContentFormat());
                links = new LinkObject[] {}; // empty list
            } else {
                links = LinkObject.parse(coapResponse.getPayload());
            }
            lwM2mresponse = new DiscoverResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value), links);
            break;
        case NOT_FOUND:
        case UNAUTHORIZED:
        case METHOD_NOT_ALLOWED:
            lwM2mresponse = new DiscoverResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
            break;
        default:
            handleUnexpectedResponseCode(request.getClient(), coapRequest, coapResponse);
        }
    }

    @Override
    public void visit(WriteRequest request) {
        switch (coapResponse.getCode()) {
        case CHANGED:
            lwM2mresponse = new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
            break;
        case BAD_REQUEST:
        case NOT_FOUND:
        case UNAUTHORIZED:
        case METHOD_NOT_ALLOWED:
            lwM2mresponse = new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
            break;
        default:
            handleUnexpectedResponseCode(request.getClient(), coapRequest, coapResponse);
        }
    }

    @Override
    public void visit(WriteAttributesRequest request) {
        switch (coapResponse.getCode()) {
        case CHANGED:
            lwM2mresponse = new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
            break;
        case BAD_REQUEST:
        case NOT_FOUND:
        case UNAUTHORIZED:
        case METHOD_NOT_ALLOWED:
            lwM2mresponse = new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
            break;
        default:
            handleUnexpectedResponseCode(request.getClient(), coapRequest, coapResponse);
        }
    }

    @Override
    public void visit(ExecuteRequest request) {
        switch (coapResponse.getCode()) {
        case CHANGED:
            lwM2mresponse = new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
            break;
        case BAD_REQUEST:
        case UNAUTHORIZED:
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            lwM2mresponse = new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
            break;
        default:
            handleUnexpectedResponseCode(request.getClient(), coapRequest, coapResponse);
        }

    }

    @Override
    public void visit(CreateRequest request) {
        switch (coapResponse.getCode()) {
        case CREATED:
            lwM2mresponse = new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
            break;
        case BAD_REQUEST:
        case UNAUTHORIZED:
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            lwM2mresponse = new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
            break;
        default:
            handleUnexpectedResponseCode(request.getClient(), coapRequest, coapResponse);
        }
    }

    @Override
    public void visit(DeleteRequest request) {
        switch (coapResponse.getCode()) {
        case DELETED:
            lwM2mresponse = new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
            break;
        case UNAUTHORIZED:
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            lwM2mresponse = new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
            break;
        default:
            handleUnexpectedResponseCode(request.getClient(), coapRequest, coapResponse);
        }
    }

    @Override
    public void visit(ObserveRequest request) {
        switch (coapResponse.getCode()) {
        case CHANGED:
            // ignore changed response (this is probably a NOTIFY)
            lwM2mresponse = null;
            break;
        case CONTENT:
            lwM2mresponse = buildContentResponse(request.getPath(), coapResponse);
            if (coapResponse.getOptions().hasObserve()) {
                // observe request succeed so we can add and observation to registry
                CaliforniumObservation observation = new CaliforniumObservation(coapRequest, request.getClient(),
                        request.getPath());
                coapRequest.addMessageObserver(observation);
                observationRegistry.addObservation(observation);
            }
            break;
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            lwM2mresponse = new ValueResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
            break;
        default:
            handleUnexpectedResponseCode(request.getClient(), coapRequest, coapResponse);
        }
    }

    private ValueResponse buildContentResponse(LwM2mPath path, Response coapResponse) {
        ResponseCode code = ResponseCode.CONTENT;
        LwM2mNode content;
        try {
            content = LwM2mNodeDecoder.decode(coapResponse.getPayload(),
                    ContentFormat.fromCode(coapResponse.getOptions().getContentFormat()), path);
        } catch (InvalidValueException e) {
            String msg = String.format("[%s] ([%s])", e.getMessage(), e.getPath().toString());
            throw new ResourceAccessException(code, path.toString(), msg);
        }
        return new ValueResponse(code, content);
    }

    @SuppressWarnings("unchecked")
    public T getResponse() {
        return (T) lwM2mresponse;
    }

    /**
     * Throws a generic {@link ResourceAccessException} indicating that the client returned an unexpected response code.
     * 
     * @param request
     * @param coapRequest
     * @param coapResponse
     */
    private void handleUnexpectedResponseCode(Client client, Request coapRequest, Response coapResponse) {
        String msg = String.format("Client [%s] returned unexpected response code [%s]", client.getEndpoint(),
                coapResponse.getCode());
        throw new ResourceAccessException(ResponseCode.fromCoapCode(coapResponse.getCode().value),
                coapRequest.getURI(), msg);
    }
}
