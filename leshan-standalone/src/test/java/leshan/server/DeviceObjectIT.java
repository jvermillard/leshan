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

import java.io.IOException;

import leshan.server.client.LwClient;
import leshan.server.utils.ApiUtils;
import leshan.server.utils.TestUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for Device object.
 */
public class DeviceObjectIT {

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
     * Interoperability Test Case : LightweightM2M-1.0-int-201 – Querying basic information from the client in Plain
     * Text format
     *
     * PSK security mode
     */
    @Test
    public void query_basic_info_plain_text() throws IOException, InterruptedException {

        String endpoint = RandomStringUtils.randomNumeric(10);
        String psk = RandomStringUtils.randomAscii(10);

        // preload security info
        ApiUtils.createPskSecurityInfo(endpoint, endpoint, psk);

        // start client
        try (LwClient c = new LwClient()) {
            c.start(endpoint, "201-device-query-plaintext.lua", endpoint, psk); // params: identity, psk

            TestUtils.waitForRegistration(endpoint);

            // read Manufacturer
            assertEquals("Open Mobile Alliance", ApiUtils.readResource(endpoint, "3", "0", "0"));

            // read Model number
            assertEquals("Leshan model", ApiUtils.readResource(endpoint, "3", "0", "1"));

            // read Serial number
            assertEquals("458662135557", ApiUtils.readResource(endpoint, "3", "0", "2"));
        }
    }

}
