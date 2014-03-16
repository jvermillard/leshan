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
package leshan.server.lwm2m.resource;


import java.util.List;

import leshan.server.lwm2m.client.BindingMode;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.client.ClientUpdate;

import org.apache.commons.io.Charsets;
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

    private static final Logger LOG = LoggerFactory.getLogger(RegisterResource.class);

    public static final String RESOURCE_NAME = "rd";

    private final ClientRegistry registry;

    public RegisterResource(ClientRegistry registry) {
        super(RESOURCE_NAME);

        this.registry = registry;
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

        try {
            String endpoint = null;
            Long lifetime = null;
            String smsNumber = null;
            String lwVersion = null;
            BindingMode binding = null;

            for (String param : request.getOptions().getURIQueries()) {
                if (param.startsWith("ep=")) {
                    endpoint = param.substring(3);
                } else if (param.startsWith("lt=")) {
                    lifetime = Long.valueOf(param.substring(3));
                } else if (param.startsWith("sms=")) {
                    smsNumber = param.substring(4);
                } else if (param.startsWith("lwm2m=")) {
                    lwVersion = param.substring(6);
                } else if (param.startsWith("b=")) {
                    binding = BindingMode.valueOf(param.substring(2));
                }
            }

            if (endpoint == null || endpoint.isEmpty()) {
                exchange.respond( ResponseCode.BAD_REQUEST, "Client must specify an endpoint identifier" );
            } else {
                // register
                String registrationId = RegisterResource.createRegistrationId();

                String[] objectLinks = new String(request.getPayload(), Charsets.UTF_8).split(",");

                Client client = new Client(registrationId, endpoint, request.getSource(), request.getSourcePort(), lwVersion,
                        lifetime, smsNumber, binding, objectLinks);

                this.registry.registerClient(client);
                LOG.debug("New registered client: {}", client);

                exchange.setLocationPath(RESOURCE_NAME + "/" + client.getRegistrationId());
                exchange.respond(ResponseCode.CREATED);
            }
        } catch (NumberFormatException e) {
            exchange.respond(ResponseCode.BAD_REQUEST, "Lifetime parameter must be a valid number");
        }
    }

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
        String lwVersion = null;
        BindingMode binding = null;
        for (String param : request.getOptions().getURIQueries()) {
            if (param.startsWith("lt=")) {
                lifetime = Long.valueOf(param.substring(3));
            } else if (param.startsWith("sms=")) {
                smsNumber = param.substring(4);
            } else if (param.startsWith("lwm2m=")) {
                lwVersion = param.substring(6);
            } else if (param.startsWith("b=")) {
                binding = BindingMode.valueOf(param.substring(2));
            }
        }
        ClientUpdate client = new ClientUpdate(registrationId, request.getSource(), request.getSourcePort(), lwVersion,
                lifetime, smsNumber, binding, null);

        Client c = this.registry.updateClient(client);
        if (c == null) {
            exchange.respond(ResponseCode.NOT_FOUND);
        } else {
            exchange.respond(ResponseCode.CHANGED);
        }

    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        LOG.debug("DELETE received : {}", exchange.advanced().getRequest());

        Client unregistered = null;
        List<String> uri = exchange.getRequestOptions().getURIPaths();
        if (uri != null && uri.size() == 2 && RESOURCE_NAME.equals(uri.get(0))) {
            unregistered = this.registry.deregisterClient(uri.get(1));
        }

        if (unregistered != null) {
            exchange.respond(ResponseCode.DELETED);
        } else {
            LOG.debug("Invalid deregistration");
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
