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
package leshan.server.lwm2m.osgi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.client.ClientRegistryListener;
import leshan.server.lwm2m.client.ClientUpdate;
import leshan.server.lwm2m.request.LwM2mRequestSender;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGI based Implementation of leshan {@link ClientRegistry}. The clients are registered in this
 * implementation to the OSGi service registry
 *
 */
public class OsgiBasedClientRegistry implements ClientRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiBasedClientRegistry.class);

    private final BundleContext ctx;
    private final LwM2mRequestSender requestSender;
    private final ConcurrentHashMap<String, ServiceRegistration<LWM2MClientDevice>> registrations = new ConcurrentHashMap<String, ServiceRegistration<LWM2MClientDevice>>();

    public OsgiBasedClientRegistry(BundleContext bundleContext, LwM2mRequestSender requestSender) {
        ctx = bundleContext;
        this.requestSender = requestSender;
        start();
    }

    @Override
    public Client get(String endpoint) {
        ServiceRegistration<LWM2MClientDevice> sreg = registrations.get(endpoint);
        LWM2MClientDevice device = ctx.getService(sreg.getReference());
        return device.getClient();
    }

    @Override
    public Collection<Client> allClients() {
        List<Client> result = new ArrayList<>(registrations.size());

        for (ServiceRegistration<LWM2MClientDevice> lw : registrations.values()) {
            LWM2MClientDevice device = ctx.getService(lw.getReference());
            result.add(device.getClient());
        }

        return result;
    }

    @Override
    public void addListener(ClientRegistryListener listener) {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeListener(ClientRegistryListener listener) {
        // TODO Auto-generated method stub
    }

    @Override
    public Client registerClient(Client client) {

        // Instantiate LWM2MDevice as wrapper around Client object and
        // register as DEVICE in OSGi registry
        LWM2MClientDevice lwm2mclient = new LWM2MClientDevice(client, requestSender);
        registerClientAtOsgiRegistry(lwm2mclient);

        return null;
    }

    @Override
    public Client updateClient(ClientUpdate clientUpdated) {

        ServiceRegistration<LWM2MClientDevice> registration = getServiceRegistrationById(clientUpdated.getRegistrationId());
        if (registration == null) {
            LOG.warn("updateClient(); throws IllegalArgumentException(); No client registered under ID "
                    + clientUpdated.getRegistrationId());
            throw new IllegalArgumentException("No client registered under ID " + clientUpdated.getRegistrationId());
        }

        ServiceReference<LWM2MClientDevice> ref = registration.getReference();
        LWM2MClientDevice device = ctx.getService(ref);
        if (device != null) {

            LOG.debug("Updating registration for client: {}", clientUpdated);
            clientUpdated.apply(device.getClient());

            Dictionary<String, Object> newProps = device.getServiceRegistrationProperties(device.getClient());
            registration.setProperties(newProps);

            return device.getClient();
        } else {
            return null;
        }
    }

    @Override
    public Client deregisterClient(String registrationId) {

        ServiceRegistration<LWM2MClientDevice> registration = getServiceRegistrationById(registrationId);

        if (registration != null) {
            ServiceReference<LWM2MClientDevice> ref = registration.getReference();
            LWM2MClientDevice device = ctx.getService(ref);
            ctx.ungetService(ref);
            registration.unregister();

            if (registrations.remove(device.getClient().getEndpoint()) == null) {
                LOG.warn(String.format("[deregisterClient()] no Service found with endpointID = %s", device.getClient().getEndpoint()));
            }
            LOG.debug(String.format("[deregisterClient()] ungetService and unregister Client with endpointID=%s  ,id=%s", device
                    .getClient().getEndpoint(), device.getClient().getRegistrationId()));

            return device.getClient();
        }
        LOG.warn(String.format("[deregisterClient()] no Client found with registrationId = %s", registrationId));

        return null;
    }

    /**
     * register a new LWM2MClientDevice at the OSGI Service Registry with service Properties: <br>
     * LWM2M_REGISTRATION_EXPIRATION <br>
     * LWM2M_REGISTRATIONID <br>
     * LWM2M_OBJECTS <br>
     * SERVICE_PID <br>
     * DEVICE_CATEGORY</br> <br>
     * If the LWM2MClientDevice has already registered, the InetAddress is updated.<br>
     * If the ServiceRegistration object has already been unregistered, the LWM2MClientDevice will
     * be registered again.
     * 
     * @param client
     */
    private void registerClientAtOsgiRegistry(LWM2MClientDevice client) {

        if (!(registrations.containsKey(client.getClient().getEndpoint()))) {
            LOG.trace("[registerClientAtOsgiRegistry()] Register new LWM2MClientDevice at osgi ServiceRegistry with ep= "
                    + client.getClient().getEndpoint());
            registerService(client);
            LOG.trace("[registerClientAtOsgiRegistry()] origin host: " + client.getClient().getAddress().toString());

        } else {
            LOG.trace("[registerClientAtOsgiRegistry()] update a LWM2MClientDevice Servicereference while clientregistration");
            // If the LWM2M Client sends a “Register” operation to the LWM2M
            // Server even though the LWM2M Server has registration information
            // of the LWM2M Client, the LWM2M Server removes the existing
            // registration information and performs the new “Register”
            // operation. This situation happens when the LWM2M Client forgets
            // the state of the LWM2M Server (e.g., factory reset).
            ServiceRegistration<LWM2MClientDevice> reg = registrations.get(client.getClient().getEndpoint());
            try {
                ServiceReference<LWM2MClientDevice> ref = reg.getReference();
                ctx.ungetService(ref);
                registerService(client);
                LOG.trace("[registerClientAtOsgiRegistry()] changed host: " + client.getClient().getAddress().toString());
            } catch (IllegalStateException e) {
                // IllegalStateException - If this ServiceRegistration object
                // has already been unregistered
                registerService(client);
                LOG.trace("[registerClientAtOsgiRegistry()] ServiceRegistration object has already been unregistered, register again.");
            }
        }
    }

    /**
     * register the LWM2MClientDevice as service in osgi service registry.
     * 
     * @param client
     */
    private void registerService(LWM2MClientDevice client) {
        ServiceRegistration<LWM2MClientDevice> registration = ctx.registerService(LWM2MClientDevice.class, client,
                client.getServiceRegistrationProperties(client.getClient()));
        registrations.put(client.getClient().getEndpoint(), registration);
    }

    protected ServiceRegistration<LWM2MClientDevice> getServiceRegistrationById(String registrationId) {

        Collection<ServiceRegistration<LWM2MClientDevice>> all = registrations.values();
        for (ServiceRegistration<LWM2MClientDevice> sreg : all) {
            String id = (String) sreg.getReference().getProperty(Property.REGISTRATION_ID);
            if (registrationId.equals(id)) {
                return sreg;
            }
        }
        return null;
    }

    /*
     * checks the availability of a registered lwm2m-client.
     */
    private final ScheduledExecutorService schedExecutor = Executors.newScheduledThreadPool(1);

    /**
     * start the registration manager, will start regular cleanup of dead registrations.
     */
    private void start() {
        // every 60 seconds clean the registration list
        // TODO: configure period,
        ScheduledFuture<?> future = schedExecutor.scheduleAtFixedRate(new Cleaner(), 2, 60, TimeUnit.SECONDS);
        LOG.trace("start ScheduledExecutorService with Cleaner Thread, with period 60s");
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

            for (Entry<String, ServiceRegistration<LWM2MClientDevice>> e : registrations.entrySet()) {
                // force de-registration
                try {
                    ServiceReference<LWM2MClientDevice> ref = e.getValue().getReference();
                    LWM2MClientDevice lwmClient = ctx.getService(ref);
                    LOG.trace(String.format("check the availability of registered lwm2m-client:%s  ,id:%s", lwmClient.getClient()
                            .getEndpoint(), lwmClient.getClient().getRegistrationId()));
                    if ((lwmClient != null) && (!lwmClient.isAlive())) {
                        LOG.trace("[Cleaner]: client " + lwmClient.getClient().getEndpoint() + " id: "
                                + lwmClient.getClient().getRegistrationId() + "  not alive.");
                        deregisterClient(lwmClient.getClient().getRegistrationId());
                    }
                } catch (IllegalStateException ex) {
                    // IllegalStateException - If this ServiceRegistration
                    // object has
                    // already been unregistered.
                    LOG.warn("check availability IllegalStateException ");
                }
            }
        }
    }
}