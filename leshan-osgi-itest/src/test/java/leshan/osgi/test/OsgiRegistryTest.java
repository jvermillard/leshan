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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import leshan.LinkObject;
import leshan.osgi.OsgiClientDevice;
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
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * TestCases for lwm2m Interface Client Registration. These tests are executed
 * as integration test with PaxExam in 'real' OSGi environment.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OsgiRegistryTest extends TestSetupConfig {

    @Test
    public void testRegisterClientAtOsgiRegistry() throws InvalidSyntaxException {
        registerSeveralCients();
        final String id = registerClientReturnID();
        Assert.assertNotNull(findByRegistrationId(id));
    }

    @Test
    public void testUpdateClientAtOsgiRegistry() throws InvalidSyntaxException {
        // registerSeveralCients();
        final Client client = newClient();
        osgiRegistry.registerClient(client);
        ServiceReference<OsgiClientDevice> ref = findByRegistrationId(client.getRegistrationId());
        final String originalExpiry = (String) ref.getProperty(Property.REGISTRATION_EXPIRATION);

        final String updateSms = "00000000";
        final BindingMode updatebinding = BindingMode.UQS;
        final long updateLifetime = client.getLifeTimeInSec() + 50000L;
        final Map<String, String> attribs = new HashMap<>();
        final LinkObject[] objectLinks = new LinkObject[] { new LinkObject("/3/1", attribs) };

        final ClientUpdate up = new ClientUpdate(client.getRegistrationId(), client.getAddress(), client.getPort(),
                updateLifetime, updateSms, updatebinding, objectLinks, client.getRegistrationDate());
        osgiRegistry.updateClient(up);
        ref = findByRegistrationId(client.getRegistrationId());
        final OsgiClientDevice device = context.getService(ref);

        Assert.assertEquals(updatebinding, device.getClient().getBindingMode());
        Assert.assertEquals(updateSms, device.getClient().getSmsNumber());
        Assert.assertEquals(updateLifetime, device.getClient().getLifeTimeInSec());
        // Assert.assertArrayEquals(objectLinks,
        // device.getClient().getObjectLinks());
        final String updatedExpiry = (String) ref.getProperty(Property.REGISTRATION_EXPIRATION);
        Assert.assertTrue("Expiration should have been extended after updating Client",
                Long.valueOf(originalExpiry) < Long.valueOf(updatedExpiry));
    }

    private ServiceReference<OsgiClientDevice> findByRegistrationId(final String id) throws InvalidSyntaxException {
        final String query = String.format("(%s=%s)", Property.REGISTRATION_ID, id);
        final Collection<ServiceReference<OsgiClientDevice>> col = context.getServiceReferences(
                OsgiClientDevice.class, query);
        if (!col.isEmpty()) {
            return col.iterator().next();
        } else {
            return null;
        }
    }

    @Test
    public void testDeregisterClientAtOsgiRegistry() throws InvalidSyntaxException {
        registerSeveralCients();
        final String id = registerClientReturnID();
        osgiRegistry.deregisterClient(id);
        Assert.assertNull(findByRegistrationId(id));
    }

    @Test
    public void getClientByEndpoint() {
        final String epID = registerSeveralCients();

        final Client c = osgiRegistry.get(epID);
        final String ep = c.getEndpoint();
        Assert.assertEquals(epID, ep);
    }

    private String registerClientReturnEp() {
        final Client client = newClient();
        osgiRegistry.registerClient(client);

        return client.getEndpoint();

    }

    private String registerClientReturnID() {
        final Client client = newClient();
        osgiRegistry.registerClient(client);

        return client.getRegistrationId();

    }

    private String registerSeveralCients() {

        registerClientReturnEp();
        registerClientReturnEp();
        final String id = registerClientReturnEp();
        registerClientReturnEp();

        return id;
    }

    private Client newClient() {

        final String registrationId = RandomStringUtils.random(10, true, true);
        final String endpoint = "test" + registrationId;
        final InetAddress address = this.address;
        final int port = 5683;
        final String lwM2mVersion = "0.1.1";
        final Long lifetime = 10000L;
        final String smsNumber = "0170" + RandomStringUtils.random(7, false, true);
        final BindingMode binding = BindingMode.U;
        final Map<String, String> attribs = new HashMap<>();
        final LinkObject[] objectLinks = new LinkObject[] { new LinkObject("/3/1", attribs),
                new LinkObject("/1", attribs), new LinkObject("/1/52343", attribs),
                new LinkObject("/13/52343", attribs), new LinkObject("/567/45", attribs) };
        final Date registrationDate = new Date();
        final Client c = new Client(registrationId, endpoint, address, port, lwM2mVersion, lifetime, smsNumber,
                binding, objectLinks, registrationDate, InetSocketAddress.createUnresolved("localhost", 5683));

        return c;
    }

}