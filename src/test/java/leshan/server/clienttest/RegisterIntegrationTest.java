/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
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
