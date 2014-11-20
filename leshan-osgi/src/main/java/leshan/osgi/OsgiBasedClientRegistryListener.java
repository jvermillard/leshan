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

import java.util.Dictionary;
import java.util.Hashtable;

import leshan.server.client.Client;
import leshan.server.client.ClientRegistryListener;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * A OSGi based ClientRegistryListener which sent a event via {@link EventAdmin}
 * if one of the methods is called.
 */
public class OsgiBasedClientRegistryListener implements ClientRegistryListener {

    private final BundleContext ctx;

    /**
     * Creates a new OsgiBasedClientRegistryListener.
     *
     * @param bundleContext
     */
    public OsgiBasedClientRegistryListener(final BundleContext bundleContext) {
        ctx = bundleContext;
    }

    /**
     * Invoked when a new client has been registered on the server. <br>
     * Sent a CLIENT_UNREGISTERED via {@link EventAdmin}
     *
     * @param client
     */
    @Override
    public void registered(final Client client) {
        sendEvent(Property.REGISTERED_EVENT, client);
    }

    /**
     * Invoked when a client has been updated. <br>
     * Sent a CLIENT_UPDATED via {@link EventAdmin}
     *
     * @param clientUpdated the client after the update
     */
    @Override
    public void updated(final Client clientUpdated) {
        sendEvent(Property.UPDATED_EVENT, clientUpdated);

    }

    /**
     * Invoked when a new client has been unregistered from the server. <br>
     * Sent a CLIENT_REGISTERED via {@link EventAdmin}
     *
     * @param client
     */
    @Override
    public void unregistered(final Client client) {
        sendEvent(Property.UNREGISTERED_EVENT, client);

    }

    private void sendEvent(final String topic, final Client client) {

        final ServiceReference<?> ref = ctx.getServiceReference(EventAdmin.class.getName());
        final Dictionary<String, Object> properties = new Hashtable<String, Object>();

        properties.put(Property.CLIENT, client);

        final Event notifyEvent = new Event(topic, properties);

        final EventAdmin eventAdmin = (EventAdmin) ctx.getService(ref);
        eventAdmin.postEvent(notifyEvent);
    }
}
