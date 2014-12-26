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

import java.net.InetSocketAddress;
import java.util.List;

import leshan.LinkObject;
import leshan.core.response.ClientResponse;
import leshan.core.response.RegisterResponse;
import leshan.server.client.BindingMode;
import leshan.server.client.ClientRegistry;
import leshan.server.registration.RegistrationHandler;
import leshan.server.request.DeregisterRequest;
import leshan.server.request.RegisterRequest;
import leshan.server.request.UpdateRequest;
import leshan.util.Validate;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A CoAP {@link Resource} in charge of handling clients registration requests.
 * <p>
 * This resource is the entry point of the Resource Directory ("/rd"). Each new client is added to the
 * {@link ClientRegistry}.
 * </p>
 */
public class RegisterResource extends CoapResource {

    private static final String QUERY_PARAM_ENDPOINT = "ep=";

    private static final String QUERY_PARAM_BINDING_MODE = "b=";

    private static final String QUERY_PARAM_LWM2M_VERSION = "lwm2m=";

    private static final String QUERY_PARAM_SMS = "sms=";

    private static final String QUERY_PARAM_LIFETIME = "lt=";

    private static final Logger LOG = LoggerFactory.getLogger(RegisterResource.class);

    public static final String RESOURCE_NAME = "rd";

    private final RegistrationHandler registrationHandler;

    public RegisterResource(RegistrationHandler registrationHandler) {
        super(RESOURCE_NAME);

        this.registrationHandler = registrationHandler;
        getAttributes().addResourceType("core.rd");
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        Request request = exchange.advanced().getRequest();

        LOG.debug("POST received : {}", request);

        // The LW M2M spec (section 8.2) mandates the usage of Confirmable
        // messages
        if (!Type.CON.equals(request.getType())) {
            exchange.respond(ResponseCode.BAD_REQUEST);
            return;
        }

        // Create LwM2m request from CoAP request
        // --------------------------------
        // TODO: assert content media type is APPLICATION LINK FORMAT?
        String endpoint = null;
        Long lifetime = null;
        String smsNumber = null;
        String lwVersion = null;
        BindingMode binding = null;
        LinkObject[] objectLinks = null;
        // Get Params
        for (String param : request.getOptions().getUriQuery()) {
            if (param.startsWith(QUERY_PARAM_ENDPOINT)) {
                endpoint = param.substring(3);
            } else if (param.startsWith(QUERY_PARAM_LIFETIME)) {
                lifetime = Long.valueOf(param.substring(3));
            } else if (param.startsWith(QUERY_PARAM_SMS)) {
                smsNumber = param.substring(4);
            } else if (param.startsWith(QUERY_PARAM_LWM2M_VERSION)) {
                lwVersion = param.substring(6);
            } else if (param.startsWith(QUERY_PARAM_BINDING_MODE)) {
                binding = BindingMode.valueOf(param.substring(2));
            }
        }
        // Get object Links
        if (request.getPayload() != null) {
            objectLinks = LinkObject.parse(request.getPayload());
        }
        // Which end point did the client post this request to?
        InetSocketAddress registrationEndpoint = exchange.advanced().getEndpoint().getAddress();
        // Get Security info
        String pskIdentity = null;
        if (exchange.advanced().getEndpoint() instanceof SecureEndpoint)
            pskIdentity = ((SecureEndpoint) exchange.advanced().getEndpoint()).getPskIdentity(request);

        RegisterRequest registerRequest = new RegisterRequest(endpoint, lifetime, lwVersion, binding, smsNumber,
                objectLinks, request.getSource(), request.getSourcePort(), registrationEndpoint, pskIdentity);

        // Handle request
        // -------------------------------
        RegisterResponse response = registrationHandler.register(registerRequest);

        // Create CoAP Response from LwM2m request
        // -------------------------------
        if (response.getCode() == leshan.ResponseCode.CREATED) {
            exchange.setLocationPath(RESOURCE_NAME + "/" + response.getRegistrationID());
            exchange.respond(ResponseCode.CREATED);
        } else {
            // TODO we lost specific message error with this refactoring
            // exchange.respond(fromLwM2mCode(response.getCode()),"error message");
            exchange.respond(fromLwM2mCode(response.getCode()));
            if (exchange.advanced().getEndpoint() instanceof SecureEndpoint) {

            }
        }
    }

    public static ResponseCode fromLwM2mCode(final leshan.ResponseCode code) {
        Validate.notNull(code);

        switch (code) {
        case CREATED:
            return ResponseCode.CREATED;
        case DELETED:
            return ResponseCode.DELETED;
        case CHANGED:
            return ResponseCode.CHANGED;
        case CONTENT:
            return ResponseCode.CONTENT;
        case BAD_REQUEST:
            return ResponseCode.BAD_REQUEST;
        case UNAUTHORIZED:
            return ResponseCode.UNAUTHORIZED;
        case NOT_FOUND:
            return ResponseCode.NOT_FOUND;
        case METHOD_NOT_ALLOWED:
            return ResponseCode.METHOD_NOT_ALLOWED;
        default:
            // TODO how can we manage CONFLICT code ...
            // } else if (code == leshan.ResponseCode.CONFLICT) {
            // //return 137;
            // } else {
            throw new IllegalArgumentException("Invalid CoAP code for LWM2M response: " + code);
        }
    }

    /**
     * Updates an existing Client registration.
     *
     * @param exchange the CoAP request containing the updated registration properties
     */
    @Override
    public void handlePUT(CoapExchange exchange) {
        Request request = exchange.advanced().getRequest();

        LOG.debug("UPDATE received : {}", request);
        if (!Type.CON.equals(request.getType())) {
            exchange.respond(ResponseCode.BAD_REQUEST);
            return;
        }

        List<String> uri = exchange.getRequestOptions().getUriPath();
        if (uri == null || uri.size() != 2 || !RESOURCE_NAME.equals(uri.get(0))) {
            exchange.respond(ResponseCode.NOT_FOUND);
            return;
        }

        // Create LwM2m request from CoAP request
        // --------------------------------
        String registrationId = uri.get(1);
        Long lifetime = null;
        String smsNumber = null;
        BindingMode binding = null;
        LinkObject[] objectLinks = null;
        for (String param : request.getOptions().getUriQuery()) {
            if (param.startsWith(QUERY_PARAM_LIFETIME)) {
                lifetime = Long.valueOf(param.substring(3));
            } else if (param.startsWith(QUERY_PARAM_SMS)) {
                smsNumber = param.substring(4);
            } else if (param.startsWith(QUERY_PARAM_BINDING_MODE)) {
                binding = BindingMode.valueOf(param.substring(2));
            }
        }
        if (request.getPayload() != null && request.getPayload().length > 0) {
            objectLinks = LinkObject.parse(request.getPayload());
        }
        UpdateRequest updateRequest = new UpdateRequest(registrationId, request.getSource(), request.getSourcePort(),
                lifetime, smsNumber, binding, objectLinks);

        // Handle request
        // -------------------------------
        ClientResponse updateResponse = registrationHandler.update(updateRequest);

        // Create CoAP Response from LwM2m request
        // -------------------------------
        exchange.respond(fromLwM2mCode(updateResponse.getCode()));
    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        LOG.debug("DELETE received : {}", exchange.advanced().getRequest());

        List<String> uri = exchange.getRequestOptions().getUriPath();

        if (uri != null && uri.size() == 2 && RESOURCE_NAME.equals(uri.get(0))) {
            DeregisterRequest deregisterRequest = new DeregisterRequest(uri.get(1));
            ClientResponse deregisterResponse = registrationHandler.deregister(deregisterRequest);
            exchange.respond(fromLwM2mCode(deregisterResponse.getCode()));
        } else {
            LOG.debug("Invalid deregistration");
            exchange.respond(ResponseCode.NOT_FOUND);
        }
    }

    /*
     * Override the default behavior so that requests to sub resources (typically /rd/{client-reg-id}) are handled by
     * /rd resource.
     */
    @Override
    public Resource getChild(String name) {
        return this;
    }
}
