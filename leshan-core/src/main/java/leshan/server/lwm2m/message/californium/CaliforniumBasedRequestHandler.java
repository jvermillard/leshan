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

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

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
import leshan.server.lwm2m.message.ReadRequest;
import leshan.server.lwm2m.message.RequestHandler;
import leshan.server.lwm2m.message.RequestTimeoutException;
import leshan.server.lwm2m.message.ResourceAccessException;
import leshan.server.lwm2m.message.ResourceSpec;
import leshan.server.lwm2m.message.ResponseCallback;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.message.WriteAttributesRequest;
import leshan.server.lwm2m.message.WriteRequest;
import leshan.server.lwm2m.observation.Observation;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.observation.ObservationRegistryImpl;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.coap.CoAP;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.coap.MessageObserverAdapter;
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
    private final Endpoint endpointSecure;

    private final int timeoutMillis;
    private final ObservationRegistry observationRegistry;

    /**
     * Sets required collaborators.
     * 
     * @param endpoint the CoAP endpoint to use for sending requests
     */
    public CaliforniumBasedRequestHandler(Endpoint endpoint, Endpoint endpointSecure) {
        this(endpoint, endpointSecure, null);
    }

    /**
     * Sets required collaborators.
     * 
     * @param endpoint the CoAP endpoint to use for sending requests
     * @param endpointSecure the CoAP DTLS endpoint to use for sending requests
     * @param observationRegistry the registry for keeping track of observed resources, if <code>null</code> an instance
     *        of {@link ObservationRegistryImpl} is used
     */
    public CaliforniumBasedRequestHandler(Endpoint endpoint, Endpoint endpointSecure,
            ObservationRegistry observationRegistry) {
        this(endpoint, endpointSecure, observationRegistry, COAP_REQUEST_TIMEOUT_MILLIS);
    }

    /**
     * Sets required collaborators.
     * 
     * @param endpoint the CoAP endpoint to use for sending requests
     * @param endpointSecure the CoAP DTLS endpoint to use for sending requests
     * @param observationRegistry the registry for keeping track of observed resources, if <code>null</code> an instance
     *        of {@link ObservationRegistryImpl} is used
     * @param timeoutMillis timeout for CoAP request
     */
    public CaliforniumBasedRequestHandler(Endpoint endpoint, Endpoint endpointSecure,
            ObservationRegistry observationRegistry, int timeoutMillis) {
        Validate.notNull(endpoint);
        Validate.notNull(endpointSecure);
        if (observationRegistry == null) {
            this.observationRegistry = new ObservationRegistryImpl();
        } else {
            this.observationRegistry = observationRegistry;
        }
        this.timeoutMillis = timeoutMillis;
        this.endpoint = endpoint;
        this.endpointSecure = endpointSecure;
    }

    // ////// READ request /////////////

    @Override
    public final ClientResponse send(ReadRequest request) {
        Request coapRequest = Request.newGet();
        setTarget(coapRequest, request.getTarget());

        Response coapResponse = send(request, coapRequest);
        return this.buildReadResponse(request, coapRequest, coapResponse);
    }

    @Override
    public void send(final ReadRequest request, final ResponseCallback callback) {
        final Request coapRequest = Request.newGet();
        setTarget(coapRequest, request.getTarget());

        coapRequest.addMessageObserver(new RequestObserver(callback) {
            @Override
            public void onResponse(Response coapResponse) {
                callback.onResponse(buildReadResponse(request, coapRequest, coapResponse));
            }
        });

        doSend(request, coapRequest);
    }

    private ClientResponse buildReadResponse(ReadRequest request, Request coapRequest, Response coapResponse) {
        switch (coapResponse.getCode()) {
        case CONTENT:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value),
                    coapResponse.getPayload(), ContentFormat.fromCode(coapResponse.getOptions().getContentFormat()));
        case UNAUTHORIZED:
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
        default:
            handleUnexpectedResponseCode(request, coapRequest, coapResponse);
            return null;
        }
    }

    // ////// DISCOVER request /////////////

    @Override
    public final ClientResponse send(DiscoverRequest request) {
        Request coapRequest = this.prepareRequest(request);

        Response coapResponse = send(request, coapRequest);
        return this.buildDiscoverResponse(request, coapRequest, coapResponse);
    }

    @Override
    public void send(final DiscoverRequest request, final ResponseCallback callback) {
        final Request coapRequest = this.prepareRequest(request);

        coapRequest.addMessageObserver(new RequestObserver(callback) {
            @Override
            public void onResponse(Response coapResponse) {
                callback.onResponse(buildDiscoverResponse(request, coapRequest, coapResponse));
            }
        });
        doSend(request, coapRequest);
    }

    private Request prepareRequest(DiscoverRequest request) {
        Request coapRequest = Request.newGet();
        setTarget(coapRequest, request.getTarget());
        coapRequest.getOptions().setAccept(MediaTypeRegistry.APPLICATION_LINK_FORMAT);
        return coapRequest;
    }

    private ClientResponse buildDiscoverResponse(DiscoverRequest request, Request coapRequest, Response coapResponse) {
        switch (coapResponse.getCode()) {
        case CONTENT:
            if (MediaTypeRegistry.APPLICATION_LINK_FORMAT != coapResponse.getOptions().getContentFormat()) {
                LOG.debug("Expected LWM2M Client [{}] to return application/link-format [{}] content but got [{}]",
                        request.getClient().getEndpoint(), MediaTypeRegistry.APPLICATION_LINK_FORMAT, coapResponse
                                .getOptions().getContentFormat());
            }
            return new DiscoverResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value),
                    coapResponse.getPayload());
        case NOT_FOUND:
        case UNAUTHORIZED:
        case METHOD_NOT_ALLOWED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
        default:
            handleUnexpectedResponseCode(request, coapRequest, coapResponse);
            return null;
        }
    }

    // ////// OBSERVE request /////////////

    @Override
    public final ClientResponse send(final ObserveRequest request) {
        Request coapRequest = this.prepareRequest(request);
        Observation observation = new CaliforniumBasedObservation(coapRequest, request.getObserver(),
                request.getTarget());
        Response coapResponse = send(request, coapRequest, EnumSet.of(CoAP.ResponseCode.CHANGED));
        ClientResponse response = this.buildObserveResponse(request, coapRequest, coapResponse);
        observationRegistry.addObservation(observation);
        return response;
    }

    @Override
    public void send(final ObserveRequest request, final ResponseCallback callback) {
        final Request coapRequest = this.prepareRequest(request);

        final Observation observation = new CaliforniumBasedObservation(coapRequest, request.getObserver(),
                request.getTarget());

        coapRequest.addMessageObserver(new RequestObserver(callback) {
            @Override
            public void onResponse(Response coapResponse) {
                // Ignore CHANGED response.
                if (coapResponse.getCode() != CoAP.ResponseCode.CHANGED) {
                    // Add observation to registry on success.
                    if (coapResponse.getCode() == CoAP.ResponseCode.CONTENT) {
                        observationRegistry.addObservation(observation);
                    }
                    callback.onResponse(buildObserveResponse(request, coapRequest, coapResponse));
                }
            }
        });
        doSend(request, coapRequest);
    }

    private Request prepareRequest(ObserveRequest request) {
        Request coapRequest = Request.newGet();
        coapRequest.setObserve();
        setTarget(coapRequest, request.getTarget());
        return coapRequest;
    }

    private ClientResponse buildObserveResponse(ObserveRequest request, Request coapRequest, Response coapResponse) {
        switch (coapResponse.getCode()) {
        case CONTENT:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value),
                    coapResponse.getPayload(), ContentFormat.fromCode(coapResponse.getOptions().getContentFormat()));
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
        default:
            handleUnexpectedResponseCode(request, coapRequest, coapResponse);
            return null;
        }
    }

    // ////// EXECUTE request /////////////

    @Override
    public final ClientResponse send(ExecRequest request) {
        Request coapRequest = this.prepareRequest(request);

        Response coapResponse = send(request, coapRequest);
        return this.buildExecResponse(request, coapRequest, coapResponse);
    }

    @Override
    public void send(final ExecRequest request, final ResponseCallback callback) {
        final Request coapRequest = this.prepareRequest(request);

        coapRequest.addMessageObserver(new RequestObserver(callback) {
            @Override
            public void onResponse(Response coapResponse) {
                callback.onResponse(buildExecResponse(request, coapRequest, coapResponse));
            }
        });

        doSend(request, coapRequest);
    }

    private Request prepareRequest(ExecRequest request) {
        Request coapRequest = Request.newPost();
        setTarget(coapRequest, request.getTarget());
        coapRequest.setPayload(request.getBytes());
        return coapRequest;
    }

    private ClientResponse buildExecResponse(ExecRequest request, Request coapRequest, Response coapResponse) {
        switch (coapResponse.getCode()) {
        case CHANGED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value),
                    coapResponse.getPayload(), ContentFormat.fromCode(coapResponse.getOptions().getContentFormat()));
        case BAD_REQUEST:
        case UNAUTHORIZED:
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
        default:
            handleUnexpectedResponseCode(request, coapRequest, coapResponse);
            return null;
        }
    }

    // ////// WRITE request /////////////

    @Override
    public final ClientResponse send(WriteRequest request) {
        Request coapRequest = this.prepareRequest(request);

        Response coapResponse = send(request, coapRequest);
        return this.buildWriteResponse(request, coapRequest, coapResponse);
    }

    @Override
    public void send(final WriteRequest request, final ResponseCallback callback) {
        final Request coapRequest = this.prepareRequest(request);

        coapRequest.addMessageObserver(new RequestObserver(callback) {
            @Override
            public void onResponse(Response coapResponse) {
                callback.onResponse(buildWriteResponse(request, coapRequest, coapResponse));
            }
        });

        doSend(request, coapRequest);
    }

    private Request prepareRequest(WriteRequest request) {
        Request coapRequest = request.isReplaceRequest() ? Request.newPut() : Request.newPost();
        coapRequest.setPayload(request.getBytes());
        setTarget(coapRequest, request.getTarget());
        return coapRequest;
    }

    private ClientResponse buildWriteResponse(WriteRequest request, Request coapRequest, Response coapResponse) {
        switch (coapResponse.getCode()) {
        case CHANGED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value),
                    coapResponse.getPayload(), ContentFormat.fromCode(coapResponse.getOptions().getContentFormat()));
        case BAD_REQUEST:
        case NOT_FOUND:
        case UNAUTHORIZED:
        case METHOD_NOT_ALLOWED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
        default:
            handleUnexpectedResponseCode(request, coapRequest, coapResponse);
            return null;
        }
    }

    // ////// WRITE ATTRIBUTE request /////////////

    @Override
    public final ClientResponse send(WriteAttributesRequest request) {
        Request coapRequest = this.prepareRequest(request);

        Response coapResponse = send(request, coapRequest);
        return this.buildWriteAttributeResponse(request, coapRequest, coapResponse);
    }

    @Override
    public void send(final WriteAttributesRequest request, final ResponseCallback callback) {
        final Request coapRequest = this.prepareRequest(request);

        coapRequest.addMessageObserver(new RequestObserver(callback) {
            @Override
            public void onResponse(Response coapResponse) {
                callback.onResponse(buildWriteAttributeResponse(request, coapRequest, coapResponse));
            }
        });

        doSend(request, coapRequest);
    }

    private Request prepareRequest(WriteAttributesRequest request) {
        Request coapRequest = Request.newPut();
        setTarget(coapRequest, request.getTarget());

        for (String query : request.getObserveSpec().toQueryParams()) {
            coapRequest.getOptions().addURIQuery(query);
        }

        return coapRequest;
    }

    private ClientResponse buildWriteAttributeResponse(WriteAttributesRequest request, Request coapRequest,
            Response coapResponse) {
        switch (coapResponse.getCode()) {
        case CHANGED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value),
                    coapResponse.getPayload(), ContentFormat.fromCode(coapResponse.getOptions().getContentFormat()));
        case BAD_REQUEST:
        case NOT_FOUND:
        case UNAUTHORIZED:
        case METHOD_NOT_ALLOWED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
        default:
            handleUnexpectedResponseCode(request, coapRequest, coapResponse);
            return null;
        }
    }

    // ////// DELETE request /////////////

    @Override
    public final ClientResponse send(DeleteRequest request) {
        Request coapRequest = Request.newDelete();
        setTarget(coapRequest, request.getTarget());

        Response coapResponse = send(request, coapRequest);
        return this.buildDeleteResponse(request, coapRequest, coapResponse);
    }

    @Override
    public void send(final DeleteRequest request, final ResponseCallback callback) {
        final Request coapRequest = Request.newDelete();
        setTarget(coapRequest, request.getTarget());

        coapRequest.addMessageObserver(new RequestObserver(callback) {
            @Override
            public void onResponse(Response coapResponse) {
                callback.onResponse(buildDeleteResponse(request, coapRequest, coapResponse));
            }
        });

        doSend(request, coapRequest);
    }

    private ClientResponse buildDeleteResponse(DeleteRequest request, Request coapRequest, Response coapResponse) {
        switch (coapResponse.getCode()) {
        case DELETED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value),
                    coapResponse.getPayload(), ContentFormat.fromCode(coapResponse.getOptions().getContentFormat()));
        case UNAUTHORIZED:
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
        default:
            handleUnexpectedResponseCode(request, coapRequest, coapResponse);
            return null;
        }
    }

    // ////// CREATE request /////////////

    @Override
    public final ClientResponse send(CreateRequest request) {
        Request coapRequest = this.prepareRequest(request);

        Response coapResponse = send(request, coapRequest);
        return this.buildCreateResponse(request, coapRequest, coapResponse);
    }

    @Override
    public void send(final CreateRequest request, final ResponseCallback callback) {
        final Request coapRequest = this.prepareRequest(request);

        coapRequest.addMessageObserver(new RequestObserver(callback) {
            @Override
            public void onResponse(Response coapResponse) {
                callback.onResponse(buildCreateResponse(request, coapRequest, coapResponse));
            }
        });
        doSend(request, coapRequest);
    }

    private Request prepareRequest(CreateRequest request) {
        Request coapRequest = Request.newPost();

        coapRequest.getOptions().setContentFormat(request.getContentFormat().getCode());
        coapRequest.setPayload(request.getBytes());
        setTarget(coapRequest, request.getTarget());

        return coapRequest;
    }

    private ClientResponse buildCreateResponse(CreateRequest request, Request coapRequest, Response coapResponse) {
        switch (coapResponse.getCode()) {
        case CREATED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value),
                    coapResponse.getPayload(), ContentFormat.fromCode(coapResponse.getOptions().getContentFormat()));
        case BAD_REQUEST:
        case UNAUTHORIZED:
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            return new ClientResponse(ResponseCode.fromCoapCode(coapResponse.getCode().value));
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
     * Sends a {@link LwM2mRequest} and waits for the response.
     * 
     * @param request the LWM2M request
     * @param coapRequest
     * @return the client's response
     * @throws ResourceAccessException if the request could not be processed by the client
     */
    protected final Response send(LwM2mRequest request, Request coapRequest) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Sending {}", request);
        }

        doSend(request, coapRequest);
        Response coapResponse = null;
        long start = System.currentTimeMillis();

        try {
            coapResponse = coapRequest.waitForResponse(this.timeoutMillis);
        } catch (InterruptedException e) {
            // no idea why some other thread should have interrupted this thread
            // but anyway, go ahead as if the timeout had been reached
            LOG.debug("Caught an unexpected InterruptedException during execution of CoAP request", e);
        }

        if (coapResponse == null) {
            request.getClient().markLastRequestFailed();
            throw new RequestTimeoutException(coapRequest.getURI(), (int) (System.currentTimeMillis() - start));
        } else {
            return coapResponse;
        }
    }

    private void doSend(LwM2mRequest request, Request coapRequest) {
        if (request.getClient().isSecure()) {
            endpointSecure.sendRequest(coapRequest);
        } else {
            endpoint.sendRequest(coapRequest);
        }
    }

    protected final Response send(LwM2mRequest request, Request coapRequest, final Set<CoAP.ResponseCode> ignoreCode) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Sending {}", request);
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Response> ref = new AtomicReference<Response>(null);
        coapRequest.addMessageObserver(new MessageObserverAdapter() {
            @Override
            public void onResponse(Response response) {
                if (!ignoreCode.contains(response.getCode())) {
                    ref.set(response);
                    latch.countDown();
                }
            }

            @Override
            public void onReject() {
                latch.countDown();
            }

            @Override
            public void onCancel() {
                latch.countDown();
            }

            @Override
            public void onTimeout() {
                latch.countDown();
            }
        });
        this.endpoint.sendRequest(coapRequest);

        try {
            latch.await();
        } catch (InterruptedException e) {
            // no idea why some other thread should have interrupted this thread
            // but anyway, go ahead as if the timeout had been reached
            LOG.debug("Caught an unexpected InterruptedException during execution of CoAP request", e);
        }
        Response coapResponse = ref.get();

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
        throw new ResourceAccessException(ResponseCode.fromCoapCode(coapResponse.getCode().value),
                coapRequest.getURI(), msg);
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

    private class RequestObserver extends MessageObserverAdapter {

        ResponseCallback callback;

        RequestObserver(ResponseCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onTimeout() {
            callback.onTimeout();
        }

        @Override
        public void onCancel() {
            callback.onCancel();
        }

        @Override
        public void onReject() {
            callback.onReject();
        }

    }

}
