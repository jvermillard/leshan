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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import leshan.server.lwm2m.client.BindingMode;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientUpdate;
import leshan.server.lwm2m.client.LinkObject;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * TestCases for lwm2m Interface Client Registration. These tests are executed as
 * integration test with PaxExam in real osgi environment.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OsgiRegistryTest extends TestSetupConfig {

    @Test
    public void checkBundleContext() {
        assertNotNull(context);
    }

    @Test
    public void checkLwm2mBundles() {
        boolean leshancoreFound = false;
        boolean leshancoreActive = false;

        boolean leshanosgiFound = false;
        boolean leshanosgiActive = false;

        final Bundle[] bundles = context.getBundles();
        for (final Bundle bundle : bundles) {
            if (bundle != null && bundle.getSymbolicName() != null) {
                if (bundle.getSymbolicName().equals("leshan-core")) {
                    leshancoreFound = true;
                    if (bundle.getState() == Bundle.ACTIVE) {
                        leshancoreActive = true;
                    }
                }
            }
            if (bundle.getSymbolicName().equals("leshan-osgi")) {
                leshanosgiFound = true;
                if (bundle.getState() == Bundle.ACTIVE) {
                    leshanosgiActive = true;
                }
            }
        }

        assertTrue(leshancoreFound);
        assertTrue(leshancoreActive);
        assertTrue(leshanosgiFound);
        assertTrue(leshanosgiActive);
    }

    @Test
    public void testLeshanClassesCanBeLoaded() {
        String[] leshanClasses = new String[]{
                "leshan.server.lwm2m.LwM2mServer",
                "leshan.server.lwm2m.bootstrap.SecurityMode",
                "leshan.server.lwm2m.client.Client",
                "leshan.server.lwm2m.impl.ClientRegistryImpl",
                "leshan.server.lwm2m.impl.californium.RegisterResource",
                "leshan.server.lwm2m.impl.node.LwM2mNodeEncoder",
                "leshan.server.lwm2m.impl.objectspec.ResourceSpec",
                "leshan.server.lwm2m.impl.security.SecureEndpoint",
                "leshan.server.lwm2m.impl.tlv.Tlv",
                "leshan.server.lwm2m.node.LwM2mNode",
                "leshan.server.lwm2m.observation.Observation",
                "leshan.server.lwm2m.request.LwM2mRequest",
                "leshan.server.lwm2m.security.SecurityInfo"
        };

        for (String clazzName : leshanClasses) {
            try {
                context.getBundle().loadClass(clazzName);
            } catch (ClassNotFoundException e) {
                Assert.fail(String.format("Should have been able to load class %s", clazzName));
            }
        }
    }

    @Test
    public void testRegisterClientAtOsgiRegistry() throws InvalidSyntaxException {
        registerSeveralCients();
        String id = registerClientReturnID();
        Assert.assertNotNull(findByRegistrationId(id));
    }

    @Test
    public void testUpdateClientAtOsgiRegistry() throws InvalidSyntaxException {
        // registerSeveralCients();
        Client client = newClient();
        osgiRegistry.registerClient(client);
        ServiceReference<LWM2MClientDevice> ref = findByRegistrationId(client.getRegistrationId());
        String originalExpiry = (String) ref.getProperty(Property.REGISTRATION_EXPIRATION);

        String updateSms = "00000000";
        BindingMode updatebinding = BindingMode.UQS;
        long updateLifetime = client.getLifeTimeInSec() + 50000L;
        Map<String, String> attribs = new HashMap<>();
        LinkObject[] objectLinks = new LinkObject[] { new LinkObject("/3/1", attribs) };

        ClientUpdate up = new ClientUpdate(client.getRegistrationId(), client.getAddress(), client.getPort(), updateLifetime, updateSms,
                updatebinding, objectLinks, client.getRegistrationDate());
        osgiRegistry.updateClient(up);
        ref = findByRegistrationId(client.getRegistrationId());
        LWM2MClientDevice device = context.getService(ref);

        Assert.assertEquals(updatebinding, device.getClient().getBindingMode());
        Assert.assertEquals(updateSms, device.getClient().getSmsNumber());
        Assert.assertEquals(updateLifetime, device.getClient().getLifeTimeInSec());
        // Assert.assertArrayEquals(objectLinks, device.getClient().getObjectLinks());
        String updatedExpiry = (String) ref.getProperty(Property.REGISTRATION_EXPIRATION);
        Assert.assertTrue("Expiration should have been extended after updating Client",
                Long.valueOf(originalExpiry) < Long.valueOf(updatedExpiry));
    }

    private ServiceReference<LWM2MClientDevice> findByRegistrationId(String id) throws InvalidSyntaxException {
        String query = String.format("(%s=%s)", Property.REGISTRATION_ID, id);
        Collection<ServiceReference<LWM2MClientDevice>> col = context.getServiceReferences(LWM2MClientDevice.class, query);
        if (!col.isEmpty()) {
            return col.iterator().next();
        } else {
            return null;
        }
    }

    @Test
    public void testDeregisterClientAtOsgiRegistry() throws InvalidSyntaxException {
        registerSeveralCients();
        String id = registerClientReturnID();
        osgiRegistry.deregisterClient(id);
        Assert.assertNull(findByRegistrationId(id));
    }

    @Test
    public void getClientByEndpoint() {
        String epID = registerSeveralCients();

        Client c = osgiRegistry.get(epID);
        String ep = c.getEndpoint();
        Assert.assertEquals(epID, ep);
    }

    private String registerClientReturnEp() {
        Client client = newClient();
        osgiRegistry.registerClient(client);

        return client.getEndpoint();

    }

    private String registerClientReturnID() {
        Client client = newClient();
        osgiRegistry.registerClient(client);

        return client.getRegistrationId();

    }

    private String registerSeveralCients() {

        registerClientReturnEp();
        registerClientReturnEp();
        String id = registerClientReturnEp();
        registerClientReturnEp();

        return id;
    }

    private Client newClient() {
        String registrationId = RandomStringUtils.random(10, true, true);
        String endpoint = "test" + registrationId;
        InetAddress address = this.address;
        int port = 5683;
        String lwM2mVersion = "0.1.1";
        Long lifetime = 10000L;
        String smsNumber = "0170" + RandomStringUtils.random(7, false, true);
        BindingMode binding = BindingMode.U;
        Map<String, String> attribs = new HashMap<>();
        LinkObject[] objectLinks = new LinkObject[] { new LinkObject("/3/1", attribs), new LinkObject("/1", attribs),
                new LinkObject("/1/52343", attribs), new LinkObject("/13/52343", attribs),
                new LinkObject("/567/45", attribs) };
        Date registrationDate = new Date();
        Client c = new Client(registrationId, endpoint, address, port, lwM2mVersion, lifetime, smsNumber, binding, objectLinks,
                registrationDate, InetSocketAddress.createUnresolved("localhost", 5683));

        return c;
    }

}