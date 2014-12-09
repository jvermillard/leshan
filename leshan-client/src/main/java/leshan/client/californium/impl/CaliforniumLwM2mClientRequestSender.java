package leshan.client.californium.impl;
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
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import leshan.LinkObject;
import leshan.client.request.LwM2mClientRequest;
import leshan.client.request.LwM2mClientRequestSender;
import leshan.client.response.OperationResponse;
import leshan.client.util.ResponseCallback;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MessageObserverAdapter;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.Endpoint;

public class CaliforniumLwM2mClientRequestSender implements LwM2mClientRequestSender{
    private static final Logger LOG = Logger.getLogger(CaliforniumLwM2mClientRequestSender.class.getCanonicalName());
    private final Set<Endpoint> endpoints;
	private final InetSocketAddress serverAddress;
	private final LinkObject[] clientObjectModel;
    
    public CaliforniumLwM2mClientRequestSender(final List<Endpoint> endpoints, final InetSocketAddress serverAddress, final LinkObject... linkObjects){
    	this.endpoints = new HashSet<Endpoint>(endpoints);
    	this.serverAddress = serverAddress;
    	this.clientObjectModel = linkObjects;
    }
  
    @Override
    public OperationResponse send(final LwM2mClientRequest request) {
        // Create the CoAP request from LwM2m request
        final CoapClientRequestBuilder coapClientRequestBuilder = new CoapClientRequestBuilder(serverAddress, clientObjectModel);
        request.accept(coapClientRequestBuilder);
        if(!coapClientRequestBuilder.areParametersValid()){
        	return OperationResponse.failure(ResponseCode.INTERNAL_SERVER_ERROR, "Request has invalid parameters.  Not sending.");
        }
        final Request coapRequest = coapClientRequestBuilder.getRequest();

        // Send CoAP request synchronously
        final SyncRequestObserver syncMessageObserver = new SyncRequestObserver(coapRequest, coapClientRequestBuilder.getTimeout()){
            @Override
            public OperationResponse buildResponse(final Response coapResponse) {
                // Build LwM2m response
                final LwM2mClientResponseBuilder lwm2mResponseBuilder = new LwM2mClientResponseBuilder(coapRequest, coapResponse,
                        CaliforniumLwM2mClientRequestSender.this);
                request.accept(lwm2mResponseBuilder);
                return lwm2mResponseBuilder.getResponse();
            }
        };
        coapRequest.addMessageObserver(syncMessageObserver);

        // Send CoAP request asynchronously
        final Endpoint endpoint = getEndpointForServer(request.getClientEndpointAddress());
        endpoint.sendRequest(coapRequest);

        // Wait for response, then return it
        return syncMessageObserver.waitForResponse();
    }

    @Override
    public void send(final LwM2mClientRequest request, final ResponseCallback responseCallback) {
        // Create the CoAP request from LwM2m request
        final CoapClientRequestBuilder coapClientRequestBuilder = new CoapClientRequestBuilder(serverAddress, clientObjectModel);
        request.accept(coapClientRequestBuilder);
        if(!coapClientRequestBuilder.areParametersValid()){
        	responseCallback.onFailure(OperationResponse.failure(ResponseCode.INTERNAL_SERVER_ERROR, "Request has invalid parameters.  Not sending."));
        	return;
        }
        final Request coapRequest = coapClientRequestBuilder.getRequest();

        // Add CoAP request callback
        coapRequest.addMessageObserver(new AsyncRequestObserver(coapRequest, responseCallback) {
        	
            @Override
            public OperationResponse buildResponse(final Response coapResponse) {
                // Build LwM2m response
                final LwM2mClientResponseBuilder lwm2mResponseBuilder = new LwM2mClientResponseBuilder(coapRequest, coapResponse,
                		CaliforniumLwM2mClientRequestSender.this);
                request.accept(lwm2mResponseBuilder);
                return lwm2mResponseBuilder.getResponse();
            }
        });

        // Send CoAP request asynchronously
        final Endpoint endpoint = getEndpointForServer(request.getClientEndpointAddress());
        endpoint.sendRequest(coapRequest);
    }

    /*
     * TODO in the future this should allow for users to set combinations of clients to servers per request.
     */
    private Endpoint getEndpointForServer(final InetSocketAddress clientEndpointAddress) {
    	if(clientEndpointAddress == null){
    		return endpoints.iterator().next();
    	}
    	
        for (final Endpoint ep : endpoints) {
            final InetSocketAddress endpointAddress = ep.getAddress();
            if (endpointAddress.equals(clientEndpointAddress)) {
                return ep;
            }
        }
        throw new IllegalStateException("can't find the client endpoint to use: "
                + clientEndpointAddress);
    }

    // ////// Request Observer Class definition/////////////

    private abstract class AbstractRequestObserver extends MessageObserverAdapter {
        Request coapRequest;

        public AbstractRequestObserver(final Request coapRequest) {
            this.coapRequest = coapRequest;
        }

        public abstract OperationResponse buildResponse(Response coapResponse);
    }

    private abstract class AsyncRequestObserver extends AbstractRequestObserver {

        ResponseCallback responseCallback;

        AsyncRequestObserver(final Request coapRequest, final ResponseCallback responseCallback) {
            super(coapRequest);
            this.responseCallback = responseCallback;
        }

        @Override
        public void onResponse(final Response coapResponse) {
            try {
                final OperationResponse lwM2mResponseT = buildResponse(coapResponse);
                if (lwM2mResponseT != null) {
                	responseCallback.onSuccess(lwM2mResponseT);
                }
            } catch (final Exception e) {
            	responseCallback.onFailure(OperationResponse.failure(ResponseCode.INTERNAL_SERVER_ERROR, e.getLocalizedMessage()));
            } finally {
                coapRequest.removeMessageObserver(this);
            }
        }

        @Override
        public void onTimeout() {
        	//TODO just have the responseCallback work with just an exception
            responseCallback.onFailure(OperationResponse.failure(ResponseCode.GATEWAY_TIMEOUT, "Request Timed Out."));
        }

        @Override
        public void onCancel() {
            responseCallback.onFailure(OperationResponse.failure(ResponseCode.FORBIDDEN, "Request Cancelled."));
        }

        @Override
        public void onReject() {
            responseCallback.onFailure(OperationResponse.failure(ResponseCode.FORBIDDEN, "Request Rejected."));
        }

    }

    private abstract class SyncRequestObserver extends AbstractRequestObserver {

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<OperationResponse> ref = new AtomicReference<OperationResponse>(null);
        AtomicBoolean coapTimeout = new AtomicBoolean(false);
        AtomicReference<RuntimeException> exception = new AtomicReference<>();

        long timeout;

        public SyncRequestObserver(final Request coapRequest, final long timeout) {
            super(coapRequest);
            this.timeout = timeout;
        }

        @Override
        public void onResponse(final Response coapResponse) {
            LOG.info("Received coap response: " + coapResponse);
            try {
                final OperationResponse lwM2mResponseT = buildResponse(coapResponse);
                if (lwM2mResponseT != null) {
                    ref.set(lwM2mResponseT);
                }
            } catch (final RuntimeException e) {
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

        public OperationResponse waitForResponse() {
            try {
                final boolean latchTimeout = latch.await(timeout, TimeUnit.MILLISECONDS);
                if (!latchTimeout || coapTimeout.get()) {
                    coapRequest.cancel();
                    if (exception.get() != null) {
                        throw exception.get();
                    } else {
                        throw new RuntimeException("Request Timed Out: " + coapRequest.getURI() + " (timeout)");
                    }
                }
            } catch (final InterruptedException e) {
                // no idea why some other thread should have interrupted this thread
                // but anyway, go ahead as if the timeout had been reached
                LOG.info("Caught an unexpected InterruptedException during execution of CoAP request " + e);
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
