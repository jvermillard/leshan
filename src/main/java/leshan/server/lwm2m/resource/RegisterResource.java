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

import leshan.server.lwm2m.client.BindingMode;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientRegistry;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.CoAP.Type;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Exchange;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

/**
 * A CoAP {@link Resource} in charge of handling clients registration requests.
 * <p>
 * This resource is the entry point of the Resource Directory ("/rd"). Each new client is added to the resource tree as
 * a {@link ClientResource} (as a child of this node).
 * </p>
 * <p>
 * This class implements the {@link ClientRegistry} interface and provides simple methods to access the list of
 * registered LW-M2M clients.
 * </p>
 */
public class RegisterResource extends ResourceBase {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterResource.class);

    private final ClientRegistry registry;

    public RegisterResource(ClientRegistry registry) {
        super("rd");
        this.registry = registry;
        getAttributes().addResourceType("core.rd");
    }

    @Override
    public void handlePOST(Exchange exchange) {
        Request request = exchange.getRequest();

        LOG.debug("POST received : {}", request);

        if (Type.CON.equals(request.getType())) {

            try {
                // register
                String registrationId = RegisterResource.createRegistrationId();

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
                String[] objectLinks = new String(request.getPayload(), "UTF-8").split(",");

                Client client = new Client(registrationId, endpoint, request.getSource(),
 request.getSourcePort(),
                        lwVersion, lifetime, smsNumber, binding, objectLinks);

                // object links

                //addObjectResources(client, objectLinks);

                //this.add(client);
                LOG.info("New registered client: {}", client);

                registry.registerClient(client);

                Response response = new Response(ResponseCode.CREATED);


                // register a handler for the newly created client
                RegisteredClientRessource clientResource = new RegisteredClientRessource(client, registry);
                add(clientResource);

                response.getOptions().addLocationPath(clientResource.getURI());
                exchange.respond(response);

            } catch (UnsupportedEncodingException e) {
                LOG.error("Invalid registration request", e);
                exchange.respond(ResponseCode.BAD_REQUEST);
            }

        } else {
            exchange.respond(ResponseCode.BAD_REQUEST);
        }
    }

    /*
    private void addObjectResources(Resource client, String[] objectLinks) {
        LOG.debug("Available objects for client {}: {}", client.getName(), objectLinks);

        for (String link : objectLinks) {

            // String valid = StringUtils.substringBetween(link.trim(), "<", ">");
            // HACK for liblwm2m client
            String valid = link.trim();

            // TODO rt and ct parameters

            if (valid != null) {
                Resource current = client;
                for (String objectName : valid.split("/")) {
                    Resource child = current.getChild(objectName);
                    if (child == null) {
                        child = new ObjectResource(objectName);
                        current.add(child);
                        LOG.debug("New object resource created: {}", child.getName());
                    }
                    current = child;
                }
            }
        }
    }*/

    private static String createRegistrationId() {
        return RandomStringUtils.random(10, true, true);
    }


}
