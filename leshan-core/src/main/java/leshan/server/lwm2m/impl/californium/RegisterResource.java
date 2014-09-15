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
import java.util.List;

import leshan.server.lwm2m.client.BindingMode;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientRegistrationException;
import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.client.ClientUpdate;
import leshan.server.lwm2m.client.LinkObject;
import leshan.server.lwm2m.impl.security.SecureEndpoint;
import leshan.server.lwm2m.security.SecurityInfo;
import leshan.server.lwm2m.security.SecurityStore;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.CoAP.Type;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

/**
 * A CoAP {@link Resource} in charge of handling clients registration requests.
 * <p>
 * This resource is the entry point of the Resource Directory ("/rd"). Each new client is added to the
 * {@link ClientRegistry}.
 * </p>
 */
public class RegisterResource extends ResourceBase {

    private static final String QUERY_PARAM_ENDPOINT = "ep=";

    private static final String QUERY_PARAM_BINDING_MODE = "b=";

    private static final String QUERY_PARAM_LWM2M_VERSION = "lwm2m=";

    private static final String QUERY_PARAM_SMS = "sms=";

    private static final String QUERY_PARAM_LIFETIME = "lt=";

    private static final Logger LOG = LoggerFactory.getLogger(RegisterResource.class);

    public static final String RESOURCE_NAME = "rd";

    private final ClientRegistry clientRegistry;

    private final SecurityStore securityStore;

    public RegisterResource(ClientRegistry clientRegistry, SecurityStore securityStore) {
        super(RESOURCE_NAME);

        this.clientRegistry = clientRegistry;
        this.securityStore = securityStore;
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

        // TODO: assert content media type is APPLICATION LINK FORMAT?

        String endpoint = null;
        Long lifetime = null;
        String smsNumber = null;
        String lwVersion = null;
        BindingMode binding = null;
        LinkObject[] objectLinks = null;
        try {

            for (String param : request.getOptions().getURIQueries()) {
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

            if (endpoint == null || endpoint.isEmpty()) {
                exchange.respond(ResponseCode.BAD_REQUEST, "Client must specify an endpoint identifier");
            } else {
                // register
                String registrationId = RegisterResource.createRegistrationId();
                if (request.getPayload() != null) {
                    objectLinks = LinkObject.parse(request.getPayload());
                }

                // do we have security information for this client?
                SecurityInfo securityInfo = securityStore.get(endpoint);

                // which end point did the client post this request to?
                InetSocketAddress registrationEndpoint = exchange.advanced().getEndpoint().getAddress();

                // if this is a secure end-point, we must check that the registering client is using the right identity.
                if (exchange.advanced().getEndpoint() instanceof SecureEndpoint) {
                    String pskIdentity = ((SecureEndpoint) exchange.advanced().getEndpoint()).getPskIdentity(request);
                    LOG.debug("Registration request received using the secure endpoint {} with identity {}",
                            registrationEndpoint, pskIdentity);

                    if (securityInfo == null || pskIdentity == null || !pskIdentity.equals(securityInfo.getIdentity())) {
                        LOG.warn("Invalid identity for client {}: expected '{}' but was '{}'", endpoint,
                                securityInfo == null ? null : securityInfo.getIdentity(), pskIdentity);
                        exchange.respond(ResponseCode.BAD_REQUEST, "Invalid identity");

                        // kill the TLS Session
                        ((SecureEndpoint) exchange.advanced().getEndpoint()).getDTLSConnector().close(
                                new InetSocketAddress(request.getSource(), request.getSourcePort()));
                        return;

                    } else {
                        LOG.debug("authenticated client {} using DTLS PSK", endpoint);
                    }
                } else {
                    if (securityInfo != null) {
                        LOG.warn("client {} must connect using DTLS PSK", endpoint);
                        exchange.respond(ResponseCode.BAD_REQUEST, "Client must connect thru DTLS (port 5684)");
                        return;
                    }
                }

                Client client = new Client(registrationId, endpoint, request.getSource(), request.getSourcePort(),
                        lwVersion, lifetime, smsNumber, binding, objectLinks, registrationEndpoint);

                clientRegistry.registerClient(client);
                LOG.debug("New registered client: {}", client);

                exchange.setLocationPath(RESOURCE_NAME + "/" + client.getRegistrationId());
                exchange.respond(ResponseCode.CREATED);
            }
        } catch (NumberFormatException e) {
            exchange.respond(ResponseCode.BAD_REQUEST, "Lifetime parameter must be a valid number");
        } catch (ClientRegistrationException e) {
            LOG.debug("Registration failed for client " + endpoint, e);
            exchange.respond(ResponseCode.BAD_REQUEST);
        }
    }

    /**
     * Updates an existing Client registration.
     * 
     * @param exchange the CoAP request containing the updated regsitration properties
     */
    @Override
    public void handlePUT(CoapExchange exchange) {
        Request request = exchange.advanced().getRequest();

        LOG.debug("UPDATE received : {}", request);
        if (!Type.CON.equals(request.getType())) {
            exchange.respond(ResponseCode.BAD_REQUEST);
            return;
        }

        List<String> uri = exchange.getRequestOptions().getURIPaths();
        if (uri == null || uri.size() != 2 || !RESOURCE_NAME.equals(uri.get(0))) {
            exchange.respond(ResponseCode.NOT_FOUND);
            return;
        }

        String registrationId = uri.get(1);

        Long lifetime = null;
        String smsNumber = null;
        BindingMode binding = null;
        LinkObject[] objectLinks = null;

        for (String param : request.getOptions().getURIQueries()) {
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

        ClientUpdate client = new ClientUpdate(registrationId, request.getSource(), request.getSourcePort(), lifetime,
                smsNumber, binding, objectLinks);

        try {
            Client c = clientRegistry.updateClient(client);
            if (c == null) {
                exchange.respond(ResponseCode.NOT_FOUND);
            } else {
                exchange.respond(ResponseCode.CHANGED);
            }
        } catch (ClientRegistrationException e) {
            LOG.debug("Registration update failed: " + client, e);
            exchange.respond(ResponseCode.BAD_REQUEST);
        }

    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        LOG.debug("DELETE received : {}", exchange.advanced().getRequest());

        Client unregistered = null;
        List<String> uri = exchange.getRequestOptions().getURIPaths();

        try {
            if (uri != null && uri.size() == 2 && RESOURCE_NAME.equals(uri.get(0))) {
                unregistered = clientRegistry.deregisterClient(uri.get(1));
            }

            if (unregistered != null) {
                exchange.respond(ResponseCode.DELETED);
            } else {
                LOG.debug("Invalid deregistration");
                exchange.respond(ResponseCode.BAD_REQUEST);
            }

        } catch (ClientRegistrationException e) {
            LOG.debug("Deregistration failed", e);
            exchange.respond(ResponseCode.BAD_REQUEST);
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

    private static String createRegistrationId() {
        return RandomStringUtils.random(10, true, true);
    }

}
