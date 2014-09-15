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
package leshan.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import leshan.server.client.LwClient;
import leshan.server.utils.ApiUtils;
import leshan.server.utils.TestUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for Registration
 */
public class RegistrationIT {

    private LeshanMain server;

    @Before
    public void start() {
        server = new LeshanMain();
        server.start();
    }

    @After
    public void stop() {
        server.stop();
    }

    /**
     * Interoperability Test Case : LightweightM2M-1.0-int-101 – Initial Registration
     * 
     * PSK security mode
     */
    @SuppressWarnings("unchecked")
    @Test
    public void initial_registration() throws IOException, InterruptedException {

        String endpoint = RandomStringUtils.randomNumeric(10);
        String psk = RandomStringUtils.randomAscii(10);

        // preload security info
        ApiUtils.createPskSecurityInfo(endpoint, endpoint, psk);

        // start client
        try (LwClient c = new LwClient()) {
            c.start(endpoint, "101-initial-registration.lua", endpoint, psk); // params: identity, psk

            Map<String, Object> client = TestUtils.waitForRegistration(endpoint);

            assertNotNull(client.get("registrationId"));
            assertNotNull(client.get("registrationDate"));
            assertNotNull(client.get("address"));
            assertEquals("1.0", client.get("lwM2MmVersion"));
            assertEquals(86400.0, client.get("lifetime"));
            assertEquals("U", client.get("bindingMode"));

            // links
            List<String> urls = new ArrayList<>();
            List<Map<String, Object>> links = (List<Map<String, Object>>) client.get("objectLinks");
            for (Map<String, Object> link : links) {
                urls.add((String) link.get("url"));
            }
            assertTrue(urls.size() > 1);
            assertTrue(urls.contains("/3/0")); // /3 ?

            assertTrue((boolean) client.get("secure"));
        }
    }

    /**
     * Interoperability Test Case : LightweigthM2M-1.0-int-103 – Deregistration
     * 
     * No security mode
     */
    @Test
    public void deregistration() throws IOException, InterruptedException {

        String endpoint = RandomStringUtils.randomNumeric(10);

        // start client
        try (LwClient c = new LwClient()) {
            c.start(endpoint, "103-deregistration.lua");

            // check the client is registered
            TestUtils.waitForRegistration(endpoint);

            // wait for deregistration (clients should deregister after 2 seconds)
            Thread.sleep(4_000);

            // check the client is not registered anymore
            List<Map<String, Object>> clients = ApiUtils.getRegisteredClients();
            assertEquals(0, clients.size());
        }
    }

}
