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
package leshan.server.clienttest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import leshan.server.LwM2mServer;

import org.junit.Test;

import com.google.gson.Gson;

public class RegisterIntegrationTest {


    @SuppressWarnings("unchecked")
    @Test
    public void register_and_deregister() throws IOException, InterruptedException {

        LwM2mServer server = new LwM2mServer();
        server.start();

        Gson gson = new Gson();

        // no client

        String json = TestUtils.getAPI("api/clients");

        List<Map<String, Object>> clients = new ArrayList<>();


        clients = gson.fromJson(json, clients.getClass());
        //Assert.assertEquals(0, clients.size());


        try (LwClient c = new LwClient()) {

            c.start();
            // client registered?

            json = TestUtils.getAPI("api/clients");
            clients = gson.fromJson(json, clients.getClass());
            assertEquals(1, clients.size());

            Map<String, Object> m = clients.get(0);
            assertEquals("testlwm2mclient", m.get("endpoint"));
            assertNotNull(m.get("registrationId"));
            assertNotNull(m.get("registrationDate"));
            assertEquals("/0:0:0:0:0:0:0:1:5683", m.get("address"));
            assertEquals("1.0", m.get("lwM2MmVersion"));
            assertEquals(86400.0, m.get("lifetime"));

            List<String> links = new ArrayList<>();
            Collections.addAll(links, "3", " 1024/10", " 1024/11", " 1024/12");

            assertEquals(links, m.get("objectLinks"));
            c.quit();

            // client de-registered?

            json = TestUtils.getAPI("api/clients");
            System.err.println(json);
        }

        server.stop();
    }
}
