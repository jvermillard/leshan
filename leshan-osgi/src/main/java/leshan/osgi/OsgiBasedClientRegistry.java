/*
 * Copyright (c) 2014, Bosch Software Innovations GmbH
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
package leshan.osgi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import leshan.server.client.Client;
import leshan.server.client.ClientRegistry;
import leshan.server.client.ClientRegistryListener;
import leshan.server.client.ClientUpdate;
import leshan.server.request.LwM2mRequestSender;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi based Implementation of the lwm2m {@link ClientRegistry}. The clients
 * are registered in this implementation to the OSGi service registry
 *
 */
public class OsgiBasedClientRegistry implements ClientRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiBasedClientRegistry.class);

    private final BundleContext ctx;
    private final LwM2mRequestSender requestSender;
    private final Map<String, ServiceRegistration<OsgiClientDevice>> registrations = new ConcurrentHashMap<String, ServiceRegistration<OsgiClientDevice>>();
    private final List<ClientRegistryListener> crlisteners = new CopyOnWriteArrayList<>();

    /*
     * ScheduledExecutorService checks the availability of a registered
     * lwm2m-client.
     */
    private final ScheduledExecutorService schedExecutor = Executors.newScheduledThreadPool(1);

    /**
     * Constructor for new OsgiBasedClientRegistry. A ClientRegistry to register
     * Clients at OSGi Service Registry.
     *
     * @param bundleContext {@link BundleContext}
     * @param requestSender {@link LwM2mRequestSender}
     */
    public OsgiBasedClientRegistry(final BundleContext bundleContext, final LwM2mRequestSender requestSender) {
        ctx = bundleContext;
        this.requestSender = requestSender;
        start();
    }

    @Override
    public Client get(final String endpoint) {
        final ServiceRegistration<OsgiClientDevice> sreg = registrations.get(endpoint);
        final OsgiClientDevice device = ctx.getService(sreg.getReference());
        return device.getClient();
    }

    @Override
    public Collection<Client> allClients() {
        final List<Client> result = new ArrayList<>(registrations.size());

        for (final ServiceRegistration<OsgiClientDevice> lw : registrations.values()) {
            final OsgiClientDevice device = ctx.getService(lw.getReference());
            result.add(device.getClient());
        }

        return result;
    }

    @Override
    public void addListener(final ClientRegistryListener listener) {
        crlisteners.add(listener);
    }

    @Override
    public void removeListener(final ClientRegistryListener listener) {
        crlisteners.remove(listener);
    }

    @Override
    public Client registerClient(final Client client) {

        // Instantiate LWM2MDevice as wrapper around Client object and
        // register as DEVICE in OSGi registry
        final OsgiClientDevice lwm2mclient = new OsgiClientDevice(client, requestSender);
        registerClientAtOsgiRegistry(lwm2mclient);

        for (final ClientRegistryListener crl : crlisteners) {
            crl.registered(client);
        }

        return null;
    }

    @Override
    public Client updateClient(final ClientUpdate clientUpdated) {

        final ServiceRegistration<OsgiClientDevice> registration = getServiceRegistrationById(clientUpdated
                .getRegistrationId());
        if (registration == null) {
            LOG.warn(String.format(
                    "updateClient(); return null: no client is registered under the given Registration-ID %s",
                    clientUpdated.getRegistrationId()));

            return null;
        }

        final ServiceReference<OsgiClientDevice> ref = registration.getReference();
        final OsgiClientDevice device = ctx.getService(ref);
        if (device != null) {

            LOG.debug("Updating registration for client: {}", clientUpdated);
            clientUpdated.apply(device.getClient());

            final Dictionary<String, Object> newProps = device.getServiceRegistrationProperties(device.getClient());
            registration.setProperties(newProps);

            for (final ClientRegistryListener crl : crlisteners) {
                crl.updated(device.getClient());
            }

            return device.getClient();
        } else {
            LOG.warn(String.format(
                    "updateClient(); return null: no LWM2MClientDevice is found under the given Registration-ID %s",
                    clientUpdated.getRegistrationId()));

            return null;
        }
    }

    @Override
    public Client deregisterClient(final String registrationId) {

        final ServiceRegistration<OsgiClientDevice> registration = getServiceRegistrationById(registrationId);

        if (registration != null) {
            final ServiceReference<OsgiClientDevice> ref = registration.getReference();
            final OsgiClientDevice device = ctx.getService(ref);
            ctx.ungetService(ref);
            registration.unregister();

            if (registrations.remove(device.getClient().getEndpoint()) == null) {
                LOG.warn(String.format("[deregisterClient()] no Service found with endpointID = %s", device.getClient()
                        .getEndpoint()));
            }
            LOG.debug(String.format(
                    "[deregisterClient()] ungetService and unregister Client with endpointID=%s  ,id=%s", device
                            .getClient().getEndpoint(), device.getClient().getRegistrationId()));

            for (final ClientRegistryListener crl : crlisteners) {
                crl.unregistered(device.getClient());
            }

            return device.getClient();
        }
        LOG.warn(String.format("[deregisterClient()] no Client found with registrationId = %s", registrationId));

        return null;
    }

    /**
     * register a new LWM2MClientDevice at the OSGI Service Registry with
     * service Properties: <br>
     * LWM2M_REGISTRATION_EXPIRATION <br>
     * LWM2M_REGISTRATIONID <br>
     * LWM2M_OBJECTS <br>
     * SERVICE_PID <br>
     * DEVICE_CATEGORY</br> <br>
     * If the LWM2MClientDevice has already registered, the InetAddress is
     * updated.<br>
     * If the ServiceRegistration object has already been unregistered, the
     * LWM2MClientDevice will be registered again.
     *
     * @param client
     */
    private void registerClientAtOsgiRegistry(final OsgiClientDevice client) {

        if (!(registrations.containsKey(client.getClient().getEndpoint()))) {
            LOG.trace(String
                    .format("[registerClientAtOsgiRegistry()] Register new LWM2MClientDevice at osgi ServiceRegistry with ep= %s",
                            client.getClient().getEndpoint()));
            registerService(client);

            LOG.trace(String.format("[registerClientAtOsgiRegistry()] origin host: %s", client.getClient().getAddress()
                    .toString()));

        } else {
            LOG.trace("[registerClientAtOsgiRegistry()] update a LWM2MClientDevice Servicereference while clientregistration");

            // If the LWM2M Client sends a “Register” operation to the LWM2M
            // Server even though the LWM2M Server has registration information
            // of the LWM2M Client, the LWM2M Server removes the existing
            // registration information and performs the new “Register”
            // operation. This situation happens when the LWM2M Client forgets
            // the state of the LWM2M Server (e.g., factory reset).
            final ServiceRegistration<OsgiClientDevice> reg = registrations.get(client.getClient().getEndpoint());
            try {
                final ServiceReference<OsgiClientDevice> ref = reg.getReference();
                ctx.ungetService(ref);
                registerService(client);

                LOG.trace(String.format("[registerClientAtOsgiRegistry()] changed host: %s", client.getClient()
                        .getAddress().toString()));
            } catch (final IllegalStateException e) {
                // IllegalStateException - If this ServiceRegistration object
                // has already been unregistered
                registerService(client);

                LOG.warn(
                        "[registerClientAtOsgiRegistry()] ServiceRegistration object has already been unregistered, register again.",
                        e);
            }
        }
    }

    /**
     * register the LWM2MClientDevice as service in osgi service registry.
     *
     * @param client
     */
    private void registerService(final OsgiClientDevice client) {
        final ServiceRegistration<OsgiClientDevice> registration = ctx.registerService(OsgiClientDevice.class,
                client, client.getServiceRegistrationProperties(client.getClient()));
        registrations.put(client.getClient().getEndpoint(), registration);
    }

    protected ServiceRegistration<OsgiClientDevice> getServiceRegistrationById(final String registrationId) {

        final Collection<ServiceRegistration<OsgiClientDevice>> all = registrations.values();
        for (final ServiceRegistration<OsgiClientDevice> sreg : all) {
            final String id = (String) sreg.getReference().getProperty(Property.REGISTRATION_ID);
            if (registrationId.equals(id)) {
                return sreg;
            }
        }
        return null;
    }

    /**
     * start the registration manager, will start regular cleanup of dead
     * registrations.
     */
    private void start() {
        // every 5 seconds clean the registration list
        // It is also conceivable to configure period,
        final ScheduledFuture<?> future = schedExecutor.scheduleAtFixedRate(new Cleaner(), 2, 5, TimeUnit.SECONDS);
        LOG.trace("start ScheduledExecutorService with Cleaner Thread, with period 5s");
        if (future.isCancelled()) {
            LOG.trace("canceled");
        }
    }

    /**
     * Stop the underlying cleanup of the registrations.
     */
    public void stop() throws InterruptedException {
        schedExecutor.shutdownNow();
        schedExecutor.awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Cleaner Thread.
     */
    private class Cleaner implements Runnable {

        @Override
        public void run() {

            for (final Entry<String, ServiceRegistration<OsgiClientDevice>> e : registrations.entrySet()) {
                // force de-registration
                try {
                    final ServiceReference<OsgiClientDevice> ref = e.getValue().getReference();
                    final OsgiClientDevice lwmClient = ctx.getService(ref);
                    if (lwmClient != null) {
                        if (lwmClient.isAlive()) {
                            LOG.trace(String.format("[Cleaner]: client: %s, id: %s, alive", lwmClient.getClient()
                                    .getEndpoint(), lwmClient.getClient().getRegistrationId()));
                        } else {
                            deregisterClient(lwmClient.getClient().getRegistrationId());
                            LOG.trace(String.format("[Cleaner]: client: %s, id:%s deregisterd", lwmClient.getClient()
                                    .getEndpoint(), lwmClient.getClient().getRegistrationId()));
                        }
                    }
                } catch (final IllegalStateException ex) {
                    // IllegalStateException - If this ServiceRegistration
                    // object has already been unregistered.
                    LOG.warn(String
                            .format("[Cleaner]: Device ServiceRegistration object with endpointId %s has already been unregistered.",
                                    e.getKey()));
                }
            }
        }
    }
}
