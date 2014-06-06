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
package leshan.server.lwm2m.message.californium;

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.RegistryListener;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.CreateRequest;
import leshan.server.lwm2m.message.DeleteRequest;
import leshan.server.lwm2m.message.DiscoverRequest;
import leshan.server.lwm2m.message.DiscoverResponse;
import leshan.server.lwm2m.message.ExecRequest;
import leshan.server.lwm2m.message.LwM2mRequest;
import leshan.server.lwm2m.message.ObserveRequest;
import leshan.server.lwm2m.message.ObserveResponse;
import leshan.server.lwm2m.message.OperationType;
import leshan.server.lwm2m.message.ReadRequest;
import leshan.server.lwm2m.message.RequestHandler;
import leshan.server.lwm2m.message.RequestTimeoutException;
import leshan.server.lwm2m.message.ResourceAccessException;
import leshan.server.lwm2m.message.ResourceSpec;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.message.WriteAttributesRequest;
import leshan.server.lwm2m.message.WriteRequest;
import leshan.server.lwm2m.observation.ObservationRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Endpoint;

/**
 * A handler in charge of sending server-initiated requests to registered clients.
 * 
 * This class also implements {@link RegistryListener} in order to being able to cancel existing observations of clients
 * unregistering from the server, thus freeing up resources.
 */
public final class CaliforniumBasedRequestHandler implements RequestHandler, RegistryListener {

    private static final Logger LOG = LoggerFactory.getLogger(CaliforniumBasedRequestHandler.class);
    private static final int COAP_REQUEST_TIMEOUT_MILLIS = 5000;

    private final Endpoint endpoint;
    private final int timeoutMillis;
    private final ObservationRegistry observationRegistry;

    /**
     * Sets required collaborators.
     * 
     * @param endpoint the CoAP endpoint to use for sending requests
     * @throws NullPointerException if the endpoint is <code>null</code>
     */
    public CaliforniumBasedRequestHandler(Endpoint endpoint) {
        this(endpoint, null);
    }

    /**
     * Sets required collaborators.
     * 
     * @param endpoint the CoAP endpoint to use for sending requests
     * @param observationRegistry the registry for keeping track of observed resources, if <code>null</code> an instance
     *        of {@link InMemoryObservationRegistry} is used
     * @throws NullPointerException if the endpoint is <code>null</code>
     */
    public CaliforniumBasedRequestHandler(Endpoint endpoint, ObservationRegistry observationRegistry) {
        this(endpoint, observationRegistry, COAP_REQUEST_TIMEOUT_MILLIS);
    }

    /**
     * Sets required collaborators.
     * 
     * @param endpoint the CoAP endpoint to use for sending requests
     * @param observationRegistry the registry for keeping track of observed resources, if <code>null</code> an instance
     *        of {@link InMemoryObservationRegistry} is used
     * @param timeoutMillis timeout for CoAP request
     * @throws NullPointerException if the endpoint is <code>null</code>
     */
    public CaliforniumBasedRequestHandler(Endpoint endpoint, ObservationRegistry observationRegistry, int timeoutMillis) {
        if (endpoint == null) {
            throw new NullPointerException("CoAP Endpoint must not be null");
        }
        if (observationRegistry == null) {
            this.observationRegistry = new InMemoryObservationRegistry();
        } else {
            this.observationRegistry = observationRegistry;
        }
        this.timeoutMillis = timeoutMillis;
        this.endpoint = endpoint;
    }

    @Override
    public final ClientResponse send(ReadRequest request) {
        Request coapRequest = Request.newGet();

        setTarget(coapRequest, request.getTarget());

        Response coapResponse = send(request, coapRequest, OperationType.READ);
        switch (coapResponse.getCode()) {
        case CONTENT:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode()), coapResponse.getPayload(),
                    ContentFormat.fromCode(coapResponse.getOptions().getContentFormat()));
        case UNAUTHORIZED:
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode()));
        default:
            handleUnexpectedResponseCode(request, coapRequest, coapResponse);
            return null;
        }
    }

    @Override
    public final ClientResponse send(DiscoverRequest request) {
        Request coapRequest = Request.newGet();
        setTarget(coapRequest, request.getTarget());
        coapRequest.getOptions().setAccept(MediaTypeRegistry.APPLICATION_LINK_FORMAT);

        Response coapResponse = send(request, coapRequest, OperationType.READ);
        switch (coapResponse.getCode()) {
        case CONTENT:
            if (MediaTypeRegistry.APPLICATION_LINK_FORMAT != coapResponse.getOptions().getContentFormat()) {
                LOG.debug("Expected LWM2M Client [{}] to return application/link-format [{}] content but got [{}]",
                        request.getClient().getEndpoint(), MediaTypeRegistry.APPLICATION_LINK_FORMAT, coapResponse
                                .getOptions().getContentFormat());
            }
            return new DiscoverResponse(ResponseCode.fromCoapCode(coapResponse.getCode()), coapResponse.getPayload());
        case NOT_FOUND:
        case UNAUTHORIZED:
        case METHOD_NOT_ALLOWED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode()));
        default:
            handleUnexpectedResponseCode(request, coapRequest, coapResponse);
            return null;
        }
    }

    @Override
    public final ClientResponse send(final ObserveRequest request) {
        Request coapRequest = Request.newGet();
        coapRequest.setObserve();
        setTarget(coapRequest, request.getTarget());

        CaliforniumBasedObservation observation = new CaliforniumBasedObservation(coapRequest, request.getObserver(),
                request.getTarget());
        this.observationRegistry.addObservation(observation);

        Response coapResponse = send(request, coapRequest, OperationType.READ);
        switch (coapResponse.getCode()) {
        case CONTENT:
            return new ObserveResponse(coapResponse.getPayload(), coapResponse.getOptions().getContentFormat(),
                    observation.getId());
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode()));
        default:
            handleUnexpectedResponseCode(request, coapRequest, coapResponse);
            return null;
        }
    }

    @Override
    public final ClientResponse send(ExecRequest request) {
        Request coapRequest = Request.newPost();

        setTarget(coapRequest, request.getTarget());
        coapRequest.setPayload(request.getBytes());

        Response coapResponse = send(request, coapRequest, OperationType.EXEC);
        switch (coapResponse.getCode()) {
        case CHANGED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode()), coapResponse.getPayload(),
                    ContentFormat.fromCode(coapResponse.getOptions().getContentFormat()));
        case BAD_REQUEST:
        case UNAUTHORIZED:
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode()));

        default:
            handleUnexpectedResponseCode(request, coapRequest, coapResponse);
            return null;
        }
    }

    @Override
    public final ClientResponse send(WriteRequest request) {
        Request coapRequest = request.isReplaceRequest() ? Request.newPut() : Request.newPost();
        coapRequest.setPayload(request.getBytes());
        setTarget(coapRequest, request.getTarget());

        Response coapResponse = send(request, coapRequest, OperationType.WRITE);
        switch (coapResponse.getCode()) {
        case CHANGED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode()), coapResponse.getPayload(),
                    ContentFormat.fromCode(coapResponse.getOptions().getContentFormat()));
        case BAD_REQUEST:
        case NOT_FOUND:
        case UNAUTHORIZED:
        case METHOD_NOT_ALLOWED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode()));
        default:
            handleUnexpectedResponseCode(request, coapRequest, coapResponse);
            return null;
        }
    }

    @Override
    public final ClientResponse send(WriteAttributesRequest request) {
        Request coapRequest = Request.newPut();

        for (String query : request.getObserveSpec().toQueryParams()) {
            coapRequest.getOptions().addURIQuery(query);
        }

        setTarget(coapRequest, request.getTarget());

        Response coapResponse = send(request, coapRequest, OperationType.WRITE);
        switch (coapResponse.getCode()) {
        case CHANGED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode()), coapResponse.getPayload(),
                    ContentFormat.fromCode(coapResponse.getOptions().getContentFormat()));
        case BAD_REQUEST:
        case NOT_FOUND:
        case UNAUTHORIZED:
        case METHOD_NOT_ALLOWED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode()));
        default:
            handleUnexpectedResponseCode(request, coapRequest, coapResponse);
            return null;
        }
    }

    @Override
    public final ClientResponse send(DeleteRequest request) {
        Request coapRequest = Request.newDelete();
        setTarget(coapRequest, request.getTarget());

        Response coapResponse = send(request, coapRequest, OperationType.WRITE);
        switch (coapResponse.getCode()) {
        case DELETED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode()), coapResponse.getPayload(),
                    ContentFormat.fromCode(coapResponse.getOptions().getContentFormat()));
        case UNAUTHORIZED:
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode()));
        default:
            handleUnexpectedResponseCode(request, coapRequest, coapResponse);
            return null;
        }
    }

    @Override
    public final ClientResponse send(CreateRequest request) {
        Request coapRequest = Request.newPost();

        coapRequest.getOptions().setContentFormat(request.getContentFormat().getCode());
        coapRequest.setPayload(request.getBytes());
        setTarget(coapRequest, request.getTarget());

        Response coapResponse = send(request, coapRequest, OperationType.WRITE);
        switch (coapResponse.getCode()) {
        case CREATED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode()), coapResponse.getPayload(),
                    ContentFormat.fromCode(coapResponse.getOptions().getContentFormat()));
        case BAD_REQUEST:
        case UNAUTHORIZED:
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode()));
        default:
            handleUnexpectedResponseCode(request, coapRequest, coapResponse);
            return null;
        }
    }

    private final void setTarget(Request coapRequest, ResourceSpec target) {
        coapRequest.setDestination(target.getClient().getAddress());
        coapRequest.setDestinationPort(target.getClient().getPort());

        // objectId
        coapRequest.getOptions().addURIPath(Integer.toString(target.getObjectId()));

        // objectInstanceId
        if (target.getObjectInstanceId() == null) {
            if (target.getResourceId() != null) {
                coapRequest.getOptions().addURIPath("0"); // default instanceId
            }
        } else {
            coapRequest.getOptions().addURIPath(Integer.toString(target.getObjectInstanceId()));
        }

        // resourceId
        if (target.getResourceId() != null) {
            coapRequest.getOptions().addURIPath(Integer.toString(target.getResourceId()));
        }
    }

    /**
     * @param request the LWM2M request
     * @param coapRequest
     * @param operationType
     * @return the client's response
     * @throws ResourceAccessException if the request could not be processed by the client
     */
    protected final Response send(LwM2mRequest request, Request coapRequest, OperationType operationType) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Sending {}", request);
        }

        this.endpoint.sendRequest(coapRequest);
        Response coapResponse = null;
        try {
            coapResponse = coapRequest.waitForResponse(this.timeoutMillis);
        } catch (InterruptedException e) {
            // no idea why some other thread should have interrupted this thread
            // but anyway, go ahead as if the timeout had been reached
            LOG.debug("Caught an unexpected InterruptedException during execution of CoAP request", e);
        }

        if (coapResponse == null) {
            request.getClient().markLastRequestFailed();
            throw new RequestTimeoutException(coapRequest.getURI(), this.timeoutMillis);
        } else {
            return coapResponse;
        }
    }

    /**
     * Throws a generic {@link ResourceAccessException} indicating that the client returned an unexpected response code.
     * 
     * @param request
     * @param coapRequest
     * @param coapResponse
     */
    private void handleUnexpectedResponseCode(LwM2mRequest request, Request coapRequest, Response coapResponse) {
        String msg = String.format("Client [%s] returned unexpected response code [%s]", request.getClient()
                .getEndpoint(), coapResponse.getCode());
        LOG.debug(msg);
        throw new ResourceAccessException(ResponseCode.fromCoapCode(coapResponse.getCode()), coapRequest.getURI(), msg);
    }

    @Override
    public void registered(Client client) {
        // nothing to do
    }

    @Override
    public void unregistered(Client client) {
        // cancel all existing observations
        this.observationRegistry.cancelObservations(client);
    }

    @Override
    public void updated(Client clientUpdated) {
        // nothing to do
    }
}
