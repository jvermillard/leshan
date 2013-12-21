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

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

/**
 * A resource created when a client is registered, mean to be deleted on client de-registration.
 */
public class RegisteredClientRessource extends ResourceBase {

    private static final Logger LOG = LoggerFactory.getLogger(RegisteredClientRessource.class);

    private Client client;

    private ClientRegistry registry;

    public RegisteredClientRessource(Client client, ClientRegistry registry) {
        super(client.getRegistrationId());
        this.client = client;
        this.registry = registry;

        this.getAttributes().addAttribute(LinkFormat.END_POINT, client.getEndpoint());

    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        LOG.debug("Client {} de-registered (registartion id: {})",client.getEndpoint(), client.getRegistrationId());
        registry.deregisterClient(client.getEndpoint());
        
        // remove the resource from the tree
        delete();

        exchange.respond(ResponseCode.DELETED);

    }
}
