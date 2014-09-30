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

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.request.ClientResponse;
import leshan.server.lwm2m.request.LwM2mRequest;
import leshan.server.lwm2m.request.LwM2mRequestSender;
import leshan.server.lwm2m.request.RequestTimeoutException;
import leshan.server.lwm2m.request.ResourceAccessException;
import leshan.server.lwm2m.request.ResponseCallback;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.coap.MessageObserverAdapter;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Endpoint;

public class CaliforniumLwM2mRequestSender implements LwM2mRequestSender {

    private static final Logger LOG = LoggerFactory.getLogger(CaliforniumLwM2mRequestSender.class);
    private static final int COAP_REQUEST_TIMEOUT_MILLIS = 5000;

    private final Set<Endpoint> endpoints;
    private final ObservationRegistry observationRegistry;
    private final long timeoutMillis;

    /**
     * @param endpoints the CoAP endpoints to use for sending requests
     * @param observationRegistry the registry for keeping track of observed resources
     */
    public CaliforniumLwM2mRequestSender(Set<Endpoint> endpoints, ObservationRegistry observationRegistry) {
        this(endpoints, observationRegistry, COAP_REQUEST_TIMEOUT_MILLIS);
    }

    /**
     * @param endpoints the CoAP endpoints to use for sending requests
     * @param observationRegistry the registry for keeping track of observed resources
     * @param timeoutMillis timeout for synchronously sending of CoAP request
     */
    public CaliforniumLwM2mRequestSender(Set<Endpoint> endpoints, ObservationRegistry observationRegistry,
            long timeoutMillis) {
        Validate.notNull(endpoints);
        Validate.notNull(observationRegistry);
        this.observationRegistry = observationRegistry;
        this.endpoints = endpoints;
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public <T extends ClientResponse> T send(final LwM2mRequest<T> request) {
        // Create the CoAP request from LwM2m request
        CaliforniumCoapRequestBuilder CoapRequestBuilder = new CaliforniumCoapRequestBuilder();
        request.accept(CoapRequestBuilder);
        final Request coapRequest = CoapRequestBuilder.getRequest();

        // Send CoAP request synchronously
        SyncRequestObserver<T> syncMessageObserver = new SyncRequestObserver<T>(coapRequest, request.getClient(),
                timeoutMillis) {
            @Override
            public T buildResponse(Response coapResponse) {
                // Build LwM2m response
                CaliforniumLwM2mResponseBuilder<T> lwm2mResponseBuilder = new CaliforniumLwM2mResponseBuilder<T>(
                        coapRequest, coapResponse, observationRegistry);
                request.accept(lwm2mResponseBuilder);
                return lwm2mResponseBuilder.getResponse();
            }
        };
        coapRequest.addMessageObserver(syncMessageObserver);

        // Send CoAP request asynchronously
        Endpoint endpoint = getEndpointForClient(request.getClient());
        endpoint.sendRequest(coapRequest);

        // Wait for response, then return it
        return syncMessageObserver.waitForResponse();
    }

    @Override
    public <T extends ClientResponse> void send(final LwM2mRequest<T> request, ResponseCallback<T> callback) {
        // Create the CoAP request from LwM2m request
        CaliforniumCoapRequestBuilder CoapRequestBuilder = new CaliforniumCoapRequestBuilder();
        request.accept(CoapRequestBuilder);
        final Request coapRequest = CoapRequestBuilder.getRequest();

        // Add CoAP request callback
        coapRequest.addMessageObserver(new AsyncRequestObserver<T>(coapRequest, request.getClient(), callback) {
            @Override
            public T buildResponse(Response coapResponse) {
                // Build LwM2m response
                CaliforniumLwM2mResponseBuilder<T> lwm2mResponseBuilder = new CaliforniumLwM2mResponseBuilder<T>(
                        coapRequest, coapResponse, observationRegistry);
                request.accept(lwm2mResponseBuilder);
                return lwm2mResponseBuilder.getResponse();
            }
        });

        // Send CoAP request asynchronously
        Endpoint endpoint = getEndpointForClient(request.getClient());
        endpoint.sendRequest(coapRequest);
    }

    /**
     * Gets the CoAP endpoint that should be used to communicate with a given client.
     * 
     * @param client the client
     * @return the CoAP endpoint bound to the same network address and port that the client connected to during
     *         registration. If no such CoAP endpoint is available, the first CoAP endpoint from the list of registered
     *         endpoints is returned
     */
    private Endpoint getEndpointForClient(Client client) {
        for (Endpoint ep : endpoints) {
            InetSocketAddress endpointAddress = ep.getAddress();
            if (endpointAddress.equals(client.getRegistrationEndpointAddress())) {
                return ep;
            }
        }
        throw new IllegalStateException("can't find the client endpoint for address : "
                + client.getRegistrationEndpointAddress());
    }

    // ////// Request Observer Class definition/////////////

    private abstract class AbstractRequestObserver<T extends ClientResponse> extends MessageObserverAdapter {
        Request coapRequest;
        Client client;

        public AbstractRequestObserver(Request coapRequest, Client client) {
            this.coapRequest = coapRequest;
            this.client = client;
        }

        public abstract T buildResponse(Response coapResponse);
    }

    private abstract class AsyncRequestObserver<T extends ClientResponse> extends AbstractRequestObserver<T> {

        ResponseCallback<T> callback;

        AsyncRequestObserver(Request coapRequest, Client client, ResponseCallback<T> callback) {
            super(coapRequest, client);
            this.callback = callback;
        }

        @Override
        public void onResponse(Response coapResponse) {
            LOG.debug("Received coap response: {}", coapResponse);
            try {
                T lwM2mResponseT = buildResponse(coapResponse);
                if (lwM2mResponseT != null) {
                    callback.onResponse(lwM2mResponseT);
                }
            } catch (ResourceAccessException e) {
                callback.onError(e);
            } finally {
                coapRequest.removeMessageObserver(this);
            }
        }

        @Override
        public void onTimeout() {
            client.markLastRequestTimedout();
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

    private abstract class SyncRequestObserver<T extends ClientResponse> extends AbstractRequestObserver<T> {

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> ref = new AtomicReference<T>(null);
        AtomicBoolean coapTimeout = new AtomicBoolean(false);
        AtomicReference<ResourceAccessException> exception = new AtomicReference<>();

        long timeout;

        public SyncRequestObserver(Request coapRequest, Client client, long timeout) {
            super(coapRequest, client);
            this.timeout = timeout;
        }

        @Override
        public void onResponse(Response coapResponse) {
            LOG.debug("Received coap response: {}", coapResponse);
            try {
                T lwM2mResponseT = buildResponse(coapResponse);
                if (lwM2mResponseT != null) {
                    ref.set(lwM2mResponseT);
                }
            } catch (ResourceAccessException e) {
                exception.set(e);
            } finally {
                latch.countDown();
            }
        }

        @Override
        public void onTimeout() {
            coapTimeout.set(true);
            latch.countDown();
        }

        @Override
        public void onCancel() {
            latch.countDown();
        }

        @Override
        public void onReject() {
            latch.countDown();
        }

        public T waitForResponse() {
            try {
                boolean latchTimeout = latch.await(timeout, TimeUnit.MILLISECONDS);
                if (!latchTimeout || coapTimeout.get()) {
                    client.markLastRequestTimedout();
                    coapRequest.cancel();
                    throw new RequestTimeoutException(coapRequest.getURI(), timeout);
                }
            } catch (InterruptedException e) {
                // no idea why some other thread should have interrupted this thread
                // but anyway, go ahead as if the timeout had been reached
                LOG.debug("Caught an unexpected InterruptedException during execution of CoAP request", e);
            } finally {
                coapRequest.removeMessageObserver(this);
            }

            if (exception.get() != null) {
                throw exception.get();
            }
            return ref.get();
        }
    }
}
