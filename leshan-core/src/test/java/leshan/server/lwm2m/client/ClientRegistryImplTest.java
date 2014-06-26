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

import java.net.InetAddress;

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
    String[] objectLinks = new String[] { "</3>" };
    String registrationId = "4711";
    Client client;

    @Before
    public void setUp() throws Exception {
        this.address = InetAddress.getLocalHost();
        this.registry = new ClientRegistryImpl();

    }

    @Test
    public void testUpdateClientKeepsUnchangedProperties() {
        givenASimpleClient(this.lifetime);
        this.registry.registerClient(this.client);

        ClientUpdate clientUpdate = new ClientUpdate(this.registrationId, this.address, this.port);
        this.registry.updateClient(clientUpdate);

        Client registeredClient = this.registry.get(this.ep);
        Assert.assertEquals((long) this.lifetime, registeredClient.getLifeTimeInSec());
        Assert.assertSame(this.binding, registeredClient.getBindingMode());
        Assert.assertEquals(this.sms, registeredClient.getSmsNumber());
    }

    @Test
    public void testRegisterClientSetsTimeToLive() {
        givenASimpleClient(this.lifetime);
        this.registry.registerClient(this.client);
        Assert.assertTrue(this.client.isAlive());
    }

    @Test
    public void testUpdateClientExtendsTimeToLive() {
        givenASimpleClient(0L);
        this.registry.registerClient(this.client);
        Assert.assertFalse(this.client.isAlive());

        ClientUpdate clientUpdate = new ClientUpdate(this.registrationId, this.address, this.port, this.lifetime, null,
                null, null);
        this.registry.updateClient(clientUpdate);
        Assert.assertTrue(this.client.isAlive());
    }

    private void givenASimpleClient(Long lifetime) {
        this.client = new Client(this.registrationId, this.ep, this.address, this.port, null, lifetime, this.sms,
                this.binding, this.objectLinks, null, false);
    }
}
