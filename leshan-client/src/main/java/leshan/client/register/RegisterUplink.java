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
package leshan.client.register;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Map;

import leshan.client.LwM2mClient;
import leshan.client.Uplink;
import leshan.client.response.Callback;
import leshan.client.response.OperationResponse;
import leshan.client.util.LinkFormatUtils;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.CoAPEndpoint;

public class RegisterUplink extends Uplink {
    private static final String MESSAGE_NULL_ENDPOINT = "Provided Endpoint was Null";
    private static final String MESSAGE_BAD_OBJECTS = "Objects and Instances Passed Were Not in Valid Link Format.";
    private static final String MESSAGE_BAD_PARAMETERS = "Either the Parameters are Invalid or the Objects and Instances are Null.";
    private static final String ENDPOINT = "ep";
    private final LwM2mClient client;

    public RegisterUplink(final InetSocketAddress destination, final CoAPEndpoint origin, final LwM2mClient client) {
        super(destination, origin);
        if (client == null) {
            throw new IllegalArgumentException("Client must not be null.");
        }
        this.client = client;
    }

    public OperationResponse register(final String endpointName, final Map<String, String> parameters, final int timeout) {
        if (parameters == null || !areParametersValid(parameters)) {
            return OperationResponse.failure(ResponseCode.BAD_REQUEST, MESSAGE_BAD_PARAMETERS);
        }

        final String payload = LinkFormatUtils.payloadize(client.getObjectModel());
        if (payload == null || payload.equals("<>")) {
            return OperationResponse.failure(ResponseCode.BAD_REQUEST, MESSAGE_BAD_OBJECTS);
        }
        final Request request = createRegisterRequest(endpointName, payload);
        request.setURI(request.getURI() + "&" + leshan.client.request.Request.toQueryStringMap(parameters));

        return sendSyncRequest(timeout, request);
    }

    public void register(final String endpointName, final Map<String, String> parameters, final Callback callback) {
        if (parameters == null || !areParametersValid(parameters)) {
            callback.onFailure(OperationResponse.failure(ResponseCode.BAD_REQUEST, MESSAGE_BAD_PARAMETERS));
            return;
        }

        final String payload = LinkFormatUtils.payloadize(client.getObjectModel());
        if (payload == null || payload.equals("<>")) {
            callback.onFailure(OperationResponse.failure(ResponseCode.BAD_REQUEST, MESSAGE_BAD_OBJECTS));
            return;
        }

        final Request request = createRegisterRequest(endpointName, payload);
        request.setURI(request.getURI() + "&" + leshan.client.request.Request.toQueryStringMap(parameters));

        sendAsyncRequest(callback, request);
    }

    public void delete(final String location, final Callback callback) {
        // TODO Auto-generated method stub

    }

    public void update(final String endpointLocation, final Map<String, String> parameters, final Callback callback) {
        if (parameters == null || !areParametersValid(parameters) || parameters.isEmpty()) {
            callback.onFailure(OperationResponse.failure(ResponseCode.BAD_REQUEST, MESSAGE_BAD_PARAMETERS));
            return;
        }

        final String payload = LinkFormatUtils.payloadize(client.getObjectModel());

        final Request request = createUpdateRequest(endpointLocation, parameters);
        if (!payload.equals("<>")) {
            request.setPayload(payload);
        }

        sendAsyncRequest(callback, request);
    }

    public OperationResponse update(final String endpointLocation, final Map<String, String> parameters,
            final long timeout) {
        if (parameters == null || !areParametersValid(parameters) || parameters.isEmpty()) {
            return OperationResponse.failure(ResponseCode.BAD_REQUEST, MESSAGE_BAD_PARAMETERS);
        }

        final String payload = LinkFormatUtils.payloadize(client.getObjectModel());

        final Request request = createUpdateRequest(endpointLocation, parameters);
        if (!payload.equals("<>")) {
            request.setPayload(payload);
        }

        return sendSyncRequest(timeout, request);
    }

    public OperationResponse deregister(final String endpointLocation, final long timeout) {
        if (endpointLocation == null) {
            return OperationResponse.failure(ResponseCode.NOT_FOUND, MESSAGE_NULL_ENDPOINT);
        }

        final Request request = createDeregisterRequest(endpointLocation);

        final OperationResponse response = sendSyncRequest(timeout, request);

        origin.stop();

        return response;
    }

    public void deregister(final String endpointLocation, final Callback callback) {
        if (endpointLocation == null) {
            callback.onFailure(OperationResponse.failure(ResponseCode.NOT_FOUND, MESSAGE_NULL_ENDPOINT));
        }

        final Request request = createDeregisterRequest(endpointLocation);

        sendAsyncRequest(new Callback() {
            final Callback initializingCallback = callback;

            @Override
            public void onSuccess(final OperationResponse response) {
                initializingCallback.onSuccess(response);
                origin.stop();
            }

            @Override
            public void onFailure(final OperationResponse response) {
                initializingCallback.onFailure(response);
                origin.stop();
            }

        }, request);
    }

    public OperationResponse notify(final String todo) {
        return null;
    }

    private Request createRegisterRequest(final String endpointName, final String payload) {
        final Request request = Request.newPost();
        final RegisterEndpoint registerEndpoint = new RegisterEndpoint(getDestination(), Collections.singletonMap(
                ENDPOINT, endpointName));
        request.setURI(registerEndpoint.toString());
        request.setPayload(payload);
        return request;
    }

    private Request createUpdateRequest(final String endpointLocation, final Map<String, String> parameters) {
        final Request request = Request.newPut();
        final RegisteredEndpoint registerEndpoint = new RegisteredEndpoint(getDestination(), endpointLocation);
        request.setURI(registerEndpoint.toString());
        request.getOptions().setURIQuery(leshan.client.request.Request.toQueryStringMap(parameters));

        return request;
    }

    private Request createDeregisterRequest(final String endpointLocation) {
        final Request request = Request.newDelete();
        final RegisteredEndpoint deregisterEndpoint = new RegisteredEndpoint(getDestination(), endpointLocation);
        request.getOptions().setLocationPath(endpointLocation);
        request.setURI(deregisterEndpoint.toString());

        return request;
    }

    private boolean areParametersValid(final Map<String, String> parameters) {
        for (final Map.Entry<String, String> p : parameters.entrySet()) {
            switch (p.getKey()) {
            case "lt":
                break;
            case "lwm2m":
                break;
            case "sms":
                return false;
            case "b":
                if (!isBindingValid(p.getValue())) {
                    return false;
                }
                break;
            default:
                return false;
            }
        }

        return true;
    }

    private boolean isBindingValid(final String value) {
        if (value.equals("U")) {
            return true;
        }

        return false;
    }

}
