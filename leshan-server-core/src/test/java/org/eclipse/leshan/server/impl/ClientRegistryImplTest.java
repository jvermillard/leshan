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
package org.eclipse.leshan.server.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.eclipse.leshan.LinkObject;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.core.request.UpdateRequest;
import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.server.impl.ClientRegistryImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ClientRegistryImplTest {

    ClientRegistryImpl registry;
    String ep = "urn:endpoint";
    InetAddress address;
    int port = 23452;
    Long lifetime = 10000L;
    String sms = "0171-32423545";
    BindingMode binding = BindingMode.UQS;
    LinkObject[] objectLinks = LinkObject.parse("</3>".getBytes(org.eclipse.leshan.util.Charsets.UTF_8));
    String registrationId = "4711";
    Client client;

    @Before
    public void setUp() throws Exception {
        address = InetAddress.getLocalHost();
        registry = new ClientRegistryImpl();
    }

    @Test
    public void update_registration_keeps_properties_unchanged() {
        givenASimpleClient(lifetime);
        registry.registerClient(client);

        UpdateRequest updateRequest = new UpdateRequest(registrationId, address, port);
        Client updatedClient = registry.updateClient(updateRequest);
        Assert.assertEquals((long) lifetime, updatedClient.getLifeTimeInSec());
        Assert.assertSame(binding, updatedClient.getBindingMode());
        Assert.assertEquals(sms, updatedClient.getSmsNumber());

        Client registeredClient = registry.get(ep);
        Assert.assertEquals((long) lifetime, registeredClient.getLifeTimeInSec());
        Assert.assertSame(binding, registeredClient.getBindingMode());
        Assert.assertEquals(sms, registeredClient.getSmsNumber());
    }

    @Test
    public void client_registration_sets_time_to_live() {
        givenASimpleClient(lifetime);
        registry.registerClient(client);
        Assert.assertTrue(client.isAlive());
    }

    @Test
    public void update_registration_to_extend_time_to_live() {
        givenASimpleClient(0L);
        registry.registerClient(client);
        Assert.assertFalse(client.isAlive());

        UpdateRequest updateRequest = new UpdateRequest(registrationId, address, port, lifetime, null, null, null);
        Client updatedClient = registry.updateClient(updateRequest);
        Assert.assertTrue(updatedClient.isAlive());

        Client registeredClient = registry.get(ep);
        Assert.assertTrue(registeredClient.isAlive());
    }

    private void givenASimpleClient(Long lifetime) {
        client = new Client(registrationId, ep, address, port, null, lifetime, sms, binding, objectLinks,
                InetSocketAddress.createUnresolved("localhost", 5683));
    }
}
