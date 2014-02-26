/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package leshan.server.lwm2m.resource;

import java.io.UnsupportedEncodingException;
import java.util.List;

import leshan.server.lwm2m.client.BindingMode;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientRegistrationException;
import leshan.server.lwm2m.client.ClientRegistry;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.CoAP.Type;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
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

        if (Type.CON.equals(request.getType())
        		&& MediaTypeRegistry.APPLICATION_LINK_FORMAT == request.getOptions().getContentFormat() ) {

            // register
            String registrationId = RegisterResource.createRegistrationId();

            String endpoint = null;
            Long lifetime = null;
            String smsNumber = null;
            String lwVersion = null;
            BindingMode binding = null;

            try {

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
                
                if( endpoint == null || endpoint.isEmpty() ) {
                   exchange.respond( ResponseCode.BAD_REQUEST, "Client must specify an endpoint identifier" );
                } else {

	                String[] objectLinks = new String(request.getPayload(), "UTF-8").split(",");

                	Client client = new Client(registrationId, endpoint, request.getSource(), request.getSourcePort(),
                        	lwVersion, lifetime, smsNumber, binding, objectLinks);

                	registry.registerClient(client);
                	LOG.debug("New registered client: {}", client);

                	exchange.setLocationPath(RESOURCE_NAME + "/" + client.getRegistrationId());
                	exchange.respond(ResponseCode.CREATED);
				}
            } catch (UnsupportedEncodingException | ClientRegistrationException e) {
                LOG.debug("Registration failed for client " + endpoint, e);
                exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR);
            } catch (NumberFormatException e) {
            	exchange.respond(ResponseCode.BAD_REQUEST, "Lifetime param is not a valid number");
            }

        } else {
            exchange.respond(ResponseCode.BAD_REQUEST);
        }
    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        LOG.debug("DELETE received : {}", exchange.advanced().getRequest());

        Client unregistered = null;
        List<String> uri = exchange.getRequestOptions().getURIPaths();
        if (uri != null && uri.size() == 2 && RESOURCE_NAME.equals(uri.get(0))) {
            try {
                unregistered = registry.deregisterClient(uri.get(1));
            } catch (ClientRegistrationException e) {
                LOG.error("Deregistration failed for client with registrationId " + uri.get(1), e);
            }
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
