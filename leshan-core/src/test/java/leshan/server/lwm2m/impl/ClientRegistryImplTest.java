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
package leshan.server.lwm2m.impl;

import static org.mockito.Mockito.mock;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import leshan.server.lwm2m.client.BindingMode;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientRegistryListener;
import leshan.server.lwm2m.client.ClientUpdate;
import leshan.server.lwm2m.client.LinkObject;

import org.apache.commons.io.Charsets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ClientRegistryImplTest {

    ClientRegistryImpl registry;
    String ep = "urn:endpoint";
    InetAddress address;
    int port = 23452;
    Long lifetime = 10000L;
    String sms = "0171-32423545";
    BindingMode binding = BindingMode.UQS;
    LinkObject[] objectLinks = LinkObject.parse("</3>".getBytes(Charsets.UTF_8));
    String registrationId = "4711";
    Client client;

    @Before
    public void setUp() throws Exception {
        address = InetAddress.getLocalHost();
        registry = new ClientRegistryImpl();

    }

    @Test
    public void testUpdateClientKeepsUnchangedProperties() {
        ClientRegistryListener crl = mock(ClientRegistryListener.class);
        registry.addListener(crl);
        givenASimpleClient(lifetime);
        registry.registerClient(client);

        ClientUpdate clientUpdate = new ClientUpdate(registrationId, address, port);
        registry.updateClient(clientUpdate);

        Client registeredClient = registry.get(ep);
        Assert.assertEquals((long) lifetime, registeredClient.getLifeTimeInSec());
        Assert.assertSame(binding, registeredClient.getBindingMode());
        Assert.assertEquals(sms, registeredClient.getSmsNumber());
        Mockito.verify(crl).updated(registeredClient);
    }

    @Test
    public void testRegisterClientSetsTimeToLive() {
        givenASimpleClient(lifetime);
        registry.registerClient(client);
        Assert.assertTrue(client.isAlive());
    }

    @Test
    public void testUpdateClientExtendsTimeToLive() {
        givenASimpleClient(0L);
        registry.registerClient(client);
        Assert.assertFalse(client.isAlive());

        ClientUpdate clientUpdate = new ClientUpdate(registrationId, address, port, lifetime, null, null, null);
        registry.updateClient(clientUpdate);
        Assert.assertTrue(client.isAlive());
    }

    private void givenASimpleClient(Long lifetime) {

        client = new Client(registrationId, ep, address, port, null, lifetime, sms, binding, objectLinks, null,
                InetSocketAddress.createUnresolved("localhost", 5683));
    }
}
