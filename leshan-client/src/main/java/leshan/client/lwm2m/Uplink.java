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

import java.io.IOException;
import java.net.InetSocketAddress;

import leshan.client.lwm2m.response.Callback;
import leshan.client.lwm2m.response.OperationResponse;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MessageObserver;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Exchange;

public abstract class Uplink {

    private static final String MESSAGE_BAD_GATEWAY = "Bad Gateway on Async Callback";
    private static final String MESSAGE_GATEWAY_TIMEOUT = "Gateway Timed Out on Asynch Callback";
    private static final String MESSAGE_INTERRUPTED = "Endpoint Interrupted While Waiting for Sync Response";
    protected final CoAPEndpoint origin;
    private final InetSocketAddress destination;

    public Uplink(final InetSocketAddress destination, final CoAPEndpoint origin) {
        if (destination == null || origin == null) {
            throw new IllegalArgumentException("Destination and/or Origin cannot be null.");
        }

        this.destination = destination;
        this.origin = origin;
    }

    protected final void checkStarted(final CoAPEndpoint endpoint) {
        if (!endpoint.isStarted()) {
            try {
                endpoint.start();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void sendAsyncRequest(final Callback callback, final Request request) {
        request.addMessageObserver(new MessageObserver() {

            @Override
            public void onTimeout() {
                request.removeMessageObserver(this);
                callback.onFailure(OperationResponse.failure(ResponseCode.GATEWAY_TIMEOUT, MESSAGE_GATEWAY_TIMEOUT));
            }

            @Override
            public void onRetransmission() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onResponse(final Response response) {
                request.removeMessageObserver(this);
                if (ResponseCode.isSuccess(response.getCode())) {
                    callback.onSuccess(OperationResponse.of(response));
                } else {
                    callback.onFailure(OperationResponse.failure(response.getCode(), "Request Failed on Server "
                            + response.getOptions()));
                }
            }

            @Override
            public void onReject() {
                request.removeMessageObserver(this);
                callback.onFailure(OperationResponse.failure(ResponseCode.BAD_GATEWAY, MESSAGE_BAD_GATEWAY));
            }

            @Override
            public void onCancel() {
                request.removeMessageObserver(this);
                callback.onFailure(OperationResponse.failure(ResponseCode.BAD_GATEWAY, MESSAGE_BAD_GATEWAY));
            }

            @Override
            public void onAcknowledgement() {

            }
        });

        checkStarted(origin);
        origin.sendRequest(request);
    }

    protected void sendAsyncResponse(final Exchange exchange, final Response response, final Callback callback) {
        response.addMessageObserver(new MessageObserver() {

            @Override
            public void onTimeout() {
                response.removeMessageObserver(this);
            }

            @Override
            public void onRetransmission() {
                // TODO: Stuff
            }

            @Override
            public void onResponse(final Response response) {
                response.removeMessageObserver(this);
            }

            @Override
            public void onReject() {
                response.removeMessageObserver(this);
            }

            @Override
            public void onCancel() {
                response.removeMessageObserver(this);
            }

            @Override
            public void onAcknowledgement() {

            }

        });

        checkStarted(origin);
        exchange.sendResponse(response);
    }

    protected OperationResponse sendSyncRequest(final long timeout, final Request request) {
        checkStarted(origin);
        origin.sendRequest(request);

        try {
            final Response response = request.waitForResponse(timeout);

            if (response == null) {
                return OperationResponse.failure(ResponseCode.GATEWAY_TIMEOUT, "Timed Out Waiting For Response.");
            } else if (ResponseCode.isSuccess(response.getCode())) {
                return OperationResponse.of(response);
            } else {
                return OperationResponse.failure(response.getCode(),
                        "Request Failed on Server " + response.getOptions());
            }
        } catch (final InterruptedException e) {
            // TODO: Am I an internal server error?
            return OperationResponse.failure(ResponseCode.INTERNAL_SERVER_ERROR, MESSAGE_INTERRUPTED);
        }
    }

    protected InetSocketAddress getDestination() {
        return destination;
    }

    public void stop() {
        origin.stop();
    }
}
