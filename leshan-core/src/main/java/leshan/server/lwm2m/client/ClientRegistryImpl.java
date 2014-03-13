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
package leshan.server.lwm2m.client;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In memory client registry
 */
public class ClientRegistryImpl implements ClientRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(ClientRegistryImpl.class);

    private ConcurrentHashMap<String /* end-point */, Client> clientsByEp = new ConcurrentHashMap<>();

    private List<RegistryListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void addListener(RegistryListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(RegistryListener listener) {
        listeners.remove(listener);
    }

    @Override
    public Collection<Client> allClients() {
        return clientsByEp.values();
    }

    @Override
    public Client get(String endpoint) {
        return clientsByEp.get(endpoint);
    }

    @Override
    public Client registerClient(Client client) {
        LOG.debug("Registering new client: {}", client);

        Client previous = clientsByEp.put(client.getEndpoint(), client);
        if (previous != null) {
            for (RegistryListener l : listeners) {
                l.unregistered(previous);
            }
        }
        for (RegistryListener l : listeners) {
            l.registered(client);
        }

        return previous;
    }

    @Override
    public Client updateClient(ClientUpdate clientUpdated) {
        LOG.debug("Updating registration for client: {}", clientUpdated);
        Validate.notNull(clientUpdated.getRegistrationId());
        for (Client client : clientsByEp.values()) {
            if (clientUpdated.getRegistrationId().equals(client.getRegistrationId())) {
                // update client
                if (clientUpdated.getAddress() != null) {
                    client.setAddress(clientUpdated.getAddress());
                }

                if (clientUpdated.getPort() > 0) {
                    client.setPort(clientUpdated.getPort());
                }

                if (clientUpdated.getLwM2mVersion() != null) {
                    client.setLwM2mVersion(clientUpdated.getLwM2mVersion());
                }

                if (clientUpdated.getBindingMode() != null) {
                    client.setBindingMode(clientUpdated.getBindingMode());
                }

                if (clientUpdated.getSmsNumber() != null) {
                    client.setSmsNumber(clientUpdated.getSmsNumber());
                }
                return client;
            }
        }
        return null;
    }

    @Override
    public Client deregisterClient(String registrationId) {
        LOG.debug("Deregistering client with registrationId: {}", registrationId);
        Validate.notNull(registrationId);

        String endpoint = null;

        for (Client client : clientsByEp.values()) {
            if (registrationId.equals(client.getRegistrationId())) {
                endpoint = client.getEndpoint();
                break;
            }
        }

        Client unregistered = null;

        if (endpoint != null) {
            unregistered = clientsByEp.remove(endpoint);
            for (RegistryListener l : listeners) {
                l.unregistered(unregistered);
            }
            LOG.debug("Unregistered client: {}", unregistered);
        }

        return unregistered;
    }
}
