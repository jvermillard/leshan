/*
 * Copyright (c) 2014, Sierra Wireless
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
package leshan.server.registration;

import java.net.InetSocketAddress;

import leshan.ResponseCode;
import leshan.core.response.ClientResponse;
import leshan.core.response.RegisterResponse;
import leshan.server.client.Client;
import leshan.server.client.ClientRegistrationException;
import leshan.server.client.ClientRegistry;
import leshan.server.client.ClientUpdate;
import leshan.server.request.DeregisterRequest;
import leshan.server.request.RegisterRequest;
import leshan.server.request.UpdateRequest;
import leshan.server.security.SecurityInfo;
import leshan.server.security.SecurityStore;
import leshan.util.RandomStringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RegistrationHandler.class);

    private SecurityStore securityStore;
    private ClientRegistry clientRegistry;

    public RegistrationHandler(ClientRegistry clientRegistry, SecurityStore securityStore) {
        this.clientRegistry = clientRegistry;
        this.securityStore = securityStore;
    }

    public RegisterResponse register(RegisterRequest registerRequest) {

        if (registerRequest.getEndpointName() == null || registerRequest.getEndpointName().isEmpty()) {
            return new RegisterResponse(ResponseCode.BAD_REQUEST);
        } else {
            // register
            String registrationId = RegistrationHandler.createRegistrationId();

            // do we have security information for this client?
            SecurityInfo securityInfo = securityStore.getByEndpoint(registerRequest.getEndpointName());

            // which end point did the client post this request to?
            InetSocketAddress registrationEndpoint = registerRequest.getRegistrationEndpoint();

            // if this is a secure end-point, we must check that the registering client is using the right identity.
            if (registerRequest.isSecure()) {
                String pskIdentity = registerRequest.getPskIdentity();
                LOG.debug("Registration request received using the secure endpoint {} with identity {}",
                        registrationEndpoint, pskIdentity);

                if (securityInfo == null || pskIdentity == null || !pskIdentity.equals(securityInfo.getIdentity())) {
                    LOG.warn("Invalid identity for client {}: expected '{}' but was '{}'",
                            registerRequest.getEndpointName(),
                            securityInfo == null ? null : securityInfo.getIdentity(), pskIdentity);
                    return new RegisterResponse(ResponseCode.BAD_REQUEST);

                } else {
                    LOG.debug("authenticated client {} using DTLS PSK", registerRequest.getEndpointName());
                }
            } else {
                if (securityInfo != null) {
                    LOG.warn("client {} must connect using DTLS PSK", registerRequest.getEndpointName());
                    return new RegisterResponse(ResponseCode.BAD_REQUEST);
                }
            }

            Client client = new Client(registrationId, registerRequest.getEndpointName(),
                    registerRequest.getSourceAddress(), registerRequest.getSourcePort(),
                    registerRequest.getLwVersion(), registerRequest.getLifetime(), registerRequest.getSmsNumber(),
                    registerRequest.getBindingMode(), registerRequest.getObjectLinks(), registrationEndpoint);

            try {
                clientRegistry.registerClient(client);
            } catch (ClientRegistrationException e) {
                // TODO It's not really a BAD_REQUEST ...
                return new RegisterResponse(ResponseCode.BAD_REQUEST);
            }
            LOG.debug("New registered client: {}", client);

            return new RegisterResponse(ResponseCode.CREATED, client.getRegistrationId());
        }
    }

    // TODO We should probably rename Client Response as LwM2mResponse
    public ClientResponse update(UpdateRequest updateRequest) {
        ClientUpdate client = new ClientUpdate(updateRequest);
        try {
            Client c = clientRegistry.updateClient(client);
            if (c == null) {
                return new ClientResponse(ResponseCode.NOT_FOUND);
            } else {
                return new ClientResponse(ResponseCode.CHANGED);
            }
        } catch (ClientRegistrationException e) {
            LOG.debug("Registration update failed: " + client, e);
            return new ClientResponse(ResponseCode.BAD_REQUEST);
        }
    }

    // TODO We should probably rename Client Response as LwM2mResponse
    public ClientResponse deregister(DeregisterRequest deregisterRequest) {
        try {
            Client unregistered = clientRegistry.deregisterClient(deregisterRequest.getRegistrationID());
            if (unregistered != null) {
                return new ClientResponse(ResponseCode.DELETED);
            } else {
                LOG.debug("Invalid deregistration");
                return new ClientResponse(ResponseCode.NOT_FOUND);
            }
        } catch (ClientRegistrationException e) {
            LOG.debug("Deregistration failed", e);
            return new ClientResponse(ResponseCode.BAD_REQUEST);
        }
    }

    private static String createRegistrationId() {
        return RandomStringUtils.random(10, true, true);
    }
}
