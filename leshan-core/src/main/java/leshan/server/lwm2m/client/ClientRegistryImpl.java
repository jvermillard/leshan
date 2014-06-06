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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In memory client registry
 */
public class ClientRegistryImpl implements ClientRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(ClientRegistryImpl.class);

    private final Map<String /* end-point */, Client> clientsByEp = new ConcurrentHashMap<>();

    private final List<RegistryListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void addListener(RegistryListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(RegistryListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public Collection<Client> allClients() {
        return Collections.unmodifiableCollection(this.clientsByEp.values());
    }

    @Override
    public Client get(String endpoint) {
        return this.clientsByEp.get(endpoint);
    }

    @Override
    public Client registerClient(Client client) {
        if (client == null) {
            throw new NullPointerException("Client must not be null");
        }

        LOG.debug("Registering new client: {}", client);

        Client previous = this.clientsByEp.put(client.getEndpoint(), client);
        if (previous != null) {
            for (RegistryListener l : this.listeners) {
                l.unregistered(previous);
            }
        }
        for (RegistryListener l : this.listeners) {
            l.registered(client);
        }

        return previous;
    }

    @Override
    public Client updateClient(ClientUpdate clientUpdated) {
        if (clientUpdated == null) {
            throw new NullPointerException("Client update must not be null");
        }

        LOG.debug("Updating registration for client: {}", clientUpdated);
        Client client = findByRegistrationId(clientUpdated.getRegistrationId());
        if (client == null) {
            return null;
        } else {
            clientUpdated.apply(client);
            return client;
        }
    }

    @Override
    public Client deregisterClient(String registrationId) {
        if (registrationId == null) {
            throw new NullPointerException("Registration ID must not be null");
        }

        LOG.debug("Deregistering client with registrationId: {}", registrationId);

        Client toBeUnregistered = findByRegistrationId(registrationId);
        if (toBeUnregistered == null) {
            return null;
        } else {
            Client unregistered = this.clientsByEp.remove(toBeUnregistered.getEndpoint());
            for (RegistryListener l : this.listeners) {
                l.unregistered(unregistered);
            }
            LOG.debug("Deregistered client: {}", unregistered);
            return unregistered;
        }
    }

    private Client findByRegistrationId(String id) {
        Client result = null;
        if (id != null) {
            for (Client client : this.clientsByEp.values()) {
                if (id.equals(client.getRegistrationId())) {
                    result = client;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * start the registration manager, will start regular cleanup of dead registrations.
     */
    public void start() {
        // every 2 seconds clean the registration list
        // TODO re-consider clean-up interval: wouldn't 5 minutes do as well?
        this.schedExecutor.scheduleAtFixedRate(new Cleaner(), 2, 2, TimeUnit.SECONDS);
    }

    /**
     * Stop the underlying cleanup of the registrations.
     */
    public void stop() throws InterruptedException {
        this.schedExecutor.shutdownNow();
        this.schedExecutor.awaitTermination(5, TimeUnit.SECONDS);
    }

    private final ScheduledExecutorService schedExecutor = Executors.newScheduledThreadPool(1);

    private class Cleaner implements Runnable {

        @Override
        public void run() {
            for (Client client : ClientRegistryImpl.this.clientsByEp.values()) {
                synchronized (client) {
                    if (!client.isAlive()) {
                        // force de-registration
                        deregisterClient(client.getRegistrationId());
                    }
                }
            }
        }
    }
}
