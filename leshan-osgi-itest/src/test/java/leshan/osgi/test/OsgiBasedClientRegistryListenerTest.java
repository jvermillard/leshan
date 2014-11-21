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
package leshan.osgi.test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

import leshan.LinkObject;
import leshan.osgi.OsgiBasedClientRegistryListener;
import leshan.osgi.Property;
import leshan.server.client.BindingMode;
import leshan.server.client.Client;
import leshan.server.client.ClientUpdate;
import leshan.util.RandomStringUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 * Test for checking OsgiBasedClientRegistryListener whether an event is sent
 * correctly.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OsgiBasedClientRegistryListenerTest extends TestSetupConfig {

    @Test
    public void client_registered() {

        final TestEventHandler h = new TestEventHandler(context);
        h.registerEventListener(Property.REGISTERED_EVENT);

        final OsgiBasedClientRegistryListener listener = new OsgiBasedClientRegistryListener(context);
        osgiRegistry.addListener(listener);
        final Client client = newClient();

        try {
            osgiRegistry.registerClient(client);
        } catch (final IllegalArgumentException e) {
            Assert.assertEquals(Property.REGISTERED_EVENT, e.getMessage());
        }
    }

    @Test
    public void client_updated() {

        final Client client = newClient();
        osgiRegistry.registerClient(client);

        final String id = client.getRegistrationId();
        final ClientUpdate upclient = newClientUpdate(id);

        final TestEventHandler h = new TestEventHandler(context);
        h.registerEventListener(Property.UPDATED_EVENT);

        final OsgiBasedClientRegistryListener listener = new OsgiBasedClientRegistryListener(context);
        osgiRegistry.addListener(listener);

        try {
            osgiRegistry.updateClient(upclient);
        } catch (final IllegalArgumentException e) {
            Assert.assertEquals(Property.UPDATED_EVENT, e.getMessage());
        }
    }

    @Test
    public void client_unregistered() {

        final Client client = newClient();
        osgiRegistry.registerClient(client);

        final TestEventHandler h = new TestEventHandler(context);
        h.registerEventListener(Property.UNREGISTERED_EVENT);

        final OsgiBasedClientRegistryListener listener = new OsgiBasedClientRegistryListener(context);
        osgiRegistry.addListener(listener);

        try {
            osgiRegistry.deregisterClient(client.getRegistrationId());
        } catch (final IllegalArgumentException e) {
            Assert.assertEquals(Property.UNREGISTERED_EVENT, e.getMessage());
        }
    }

    private Client newClient() {

        final String registrationId = RandomStringUtils.random(10, true, true);
        final String endpoint = "listener" + registrationId;
        final InetAddress address = this.address;
        final int port = 5683;
        final String lwM2mVersion = "1.0.0";
        final Long lifetime = 10000L;
        final String smsNumber = "0170" + RandomStringUtils.random(7, false, true);
        final BindingMode binding = BindingMode.U;
        final LinkObject[] objectLinks = LinkObject.parse("</3>".getBytes(leshan.util.Charsets.UTF_8));
        final Date registrationDate = new Date();

        final Client c = new Client(registrationId, endpoint, address, port, lwM2mVersion, lifetime, smsNumber,
                binding, objectLinks, registrationDate, InetSocketAddress.createUnresolved("localhost", 5683));

        return c;
    }

    private ClientUpdate newClientUpdate(final String id) {

        final InetAddress address = this.address;
        final int port = 5683;

        final ClientUpdate c = new ClientUpdate(id, address, port);

        return c;
    }

    public static class TestEventHandler implements EventHandler {

        private final BundleContext context;

        public TestEventHandler(final BundleContext context) {
            this.context = context;
        }

        @Override
        public void handleEvent(final Event event) {
            throw new IllegalArgumentException(event.getTopic());
        }

        public void registerEventListener(final String topic) {

            final String[] topics = new String[] { topic.toString() };
            final Dictionary<String, String[]> props = new Hashtable<String, String[]>();

            props.put(EventConstants.EVENT_TOPIC, topics);
            context.registerService(EventHandler.class.getName(), this, props);
        }
    }
}
