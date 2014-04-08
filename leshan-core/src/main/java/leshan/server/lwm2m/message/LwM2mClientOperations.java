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
package leshan.server.lwm2m.message;

import leshan.server.lwm2m.client.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Endpoint;

/**
 * A handler in charge of sending server-initiated requests to the registered clients.
 */
public class LwM2mClientOperations implements RequestHandler {

    private static final int COAP_REQUEST_TIMEOUT_MILLIS = 5000;

    private static final Logger LOG = LoggerFactory.getLogger(LwM2mClientOperations.class);

    /** The CoAP end-point */
    private final Endpoint endpoint;

    /**
     * Sets required collaborators.
     * 
     * @param endpoint the CoAP endpoint to use for sending requests
     * @throws NullPointerException if the endpoint is <code>null</code>
     */
    public LwM2mClientOperations(Endpoint endpoint) {
        if (endpoint == null) {
            throw new NullPointerException("CoAP Endpoint must nor be null");
        }
        this.endpoint = endpoint;
    }

    @Override
    public final ClientResponse send(ReadRequest readRequest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("READ request for client {}: {}", readRequest.getClient().getEndpoint(), readRequest);
        }

        // TODO: check if client supports object ID
        // client.supportObject(Integer.toString(createRequest.getObjectId()));

        Request coapRequest = Request.newGet();

        setURIPath(coapRequest, readRequest);
        setDestination(coapRequest, readRequest.getClient());

        return sendRequest(coapRequest, OperationType.R);
    }

    @Override
    public final ClientResponse send(ExecRequest execRequest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("EXEC request for client {}: {}", execRequest.getClient().getEndpoint(), execRequest);
        }

        Request coapRequest = Request.newPost();

        setURIPath(coapRequest, execRequest);
        setDestination(coapRequest, execRequest.getClient());
        coapRequest.setPayload(execRequest.getBytes());

        return sendRequest(coapRequest, OperationType.E);

    }

    @Override
    public final ClientResponse send(WriteRequest writeRequest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("WRITE request for client {}: {}", writeRequest.getClient().getEndpoint(), writeRequest);
        }
        Request coapRequest = writeRequest.isReplaceRequest() ? Request.newPut() : Request.newPost();

        setURIPath(coapRequest, writeRequest);
        setDestination(coapRequest, writeRequest.getClient());
        coapRequest.setPayload(writeRequest.getBytes());

        return sendRequest(coapRequest, OperationType.W);
    }

    @Override
    public final ClientResponse send(CreateRequest createRequest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("CREATE request for client {}: {}", createRequest.getClient().getEndpoint(), createRequest);
        }
        Request coapRequest = Request.newPost();

        setURIPath(coapRequest, createRequest);
        setDestination(coapRequest, createRequest.getClient());
        coapRequest.getOptions().setContentFormat(createRequest.getContentFormat().getCode());
        coapRequest.setPayload(createRequest.getBytes());

        return sendRequest(coapRequest, OperationType.W);
    }

    private final void setURIPath(Request coapRequest, AbstractLwM2mRequest lwM2mRequest) {
        // objectId
        coapRequest.getOptions().addURIPath(Integer.toString(lwM2mRequest.getObjectId()));

        // objectInstanceId
        if (lwM2mRequest.getObjectInstanceId() == null) {
            if (lwM2mRequest.getResourceId() != null) {
                coapRequest.getOptions().addURIPath("0"); // default instanceId
            }
        } else {
            coapRequest.getOptions().addURIPath(Integer.toString(lwM2mRequest.getObjectInstanceId()));
        }

        // resourceId
        if (lwM2mRequest.getResourceId() != null) {
            coapRequest.getOptions().addURIPath(Integer.toString(lwM2mRequest.getResourceId()));
        }
    }

    private final void setDestination(Request coapRequest, Client client) {
        coapRequest.setDestination(client.getAddress());
        coapRequest.setDestinationPort(client.getPort());
    }

    /**
     * Sends a Lightweight M2M request to a client.
     * 
     * @param coapRequest the request
     * @param operationType the type of operation the request reflects
     * @return the response from the client or <code>null</code> if the client did not send a response within 5 seconds
     * @throws NullPointerException if any of the parameters is <code>null</code>
     * @throws ResourceAccessException if the client could not process the request
     */
    protected final ClientResponse sendRequest(Request coapRequest, OperationType operationType) {

        this.endpoint.sendRequest(coapRequest);
        Response coapResponse = null;
        try {
            coapResponse = coapRequest.waitForResponse(COAP_REQUEST_TIMEOUT_MILLIS);
        } catch (InterruptedException e) {
            // no idea why some other thread should have interrupted this thread
            // but anyway, go ahead as if the timeout had been reached
            LOG.debug("Caught an unexpected InterruptedException during execution of CoAP request", e);
        }

        if (coapResponse == null) {
            throw new RequestTimeoutException(coapRequest.getURI(), COAP_REQUEST_TIMEOUT_MILLIS);
        } else {
            switch (coapResponse.getCode()) {
            case CONTENT:
                return new ContentResponse(coapResponse.getPayload(), coapResponse.getOptions().getContentFormat());
            case CHANGED:
            case DELETED:
            case CREATED:
                return new ClientResponse(coapResponse.getCode().toString(), coapResponse.getPayload(), coapResponse
                        .getOptions().getContentFormat());
            case NOT_FOUND:
                throw new ResourceNotFoundException(coapRequest.getURI(), coapResponse.getPayloadString());
            case UNAUTHORIZED:
                throw new AuthorizationException(coapRequest.getURI(), coapResponse.getPayloadString());
            case METHOD_NOT_ALLOWED:
                throw new OperationNotSupportedException(operationType, coapResponse.getCode().value,
                        coapRequest.getURI(), coapResponse.getPayloadString());
            default:
                throw new ResourceAccessException(coapResponse.getCode().value, coapRequest.getURI(),
                        coapResponse.getPayloadString());
            }
        }
    }

}
