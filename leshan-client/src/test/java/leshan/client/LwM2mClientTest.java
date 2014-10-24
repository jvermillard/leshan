/*
 * Copyright (c) 2013, Sierra Wireless,
 * Copyright (c) 2014, Zebra Technologies,
 * 
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
package leshan.client;

import static org.junit.Assert.assertNull;
import leshan.client.bootstrap.BootstrapDownlink;

import org.eclipse.californium.core.CoapServer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LwM2mClientTest {
    @Mock
    private BootstrapDownlink fakeBootstrapDownlink;

    @Mock
    private CoapServer server;

    @Before
    public void setup() {
    }

    @Test
    public void testNothing() {
        assertNull(null);
    }

    // @Test
    // public void testLegalBootstrapUplinkCreate() {
    // final ClientObject object = new ClientObject(1,
    // new SingleResourceDefinition(0, Readabl.NOT_READABLE, Writable.NOT_WRITABLE, Executable.NOT_EXECUTABLE));
    // final LwM2mClient client = new LwM2mClient(object);
    // final BootstrapUplink uplink = client.startBootstrap(4321, InetSocketAddress.createUnresolved("localhost", 1234),
    // fakeBootstrapDownlink);
    // }
    //
    // @Test(expected=IllegalArgumentException.class)
    // public void testIllegalNullBootstrapUplinkCreate() {
    // final ClientObject object = new ClientObject(1);
    // final LwM2mClient client = new LwM2mClient(object);
    // final BootstrapUplink uplink = client.startBootstrap(4321, InetSocketAddress.createUnresolved("localhost", 1234),
    // null);
    // }
    //
    // @Test(expected=IllegalArgumentException.class)
    // public void testIllegalNoObjectsClientCreate() {
    // final ClientObject object = new ClientObject(1);
    // final LwM2mClient client = new LwM2mClient();
    // }
    //
    // @Test
    // public void testLegalRegisterUplinkCreate() {
    // final ClientObject object = new ClientObject(1,
    // new SingleResourceDefinition(0, Readabl.NOT_READABLE, Writable.NOT_WRITABLE, Executable.NOT_EXECUTABLE));
    // Mockito.when(server.getRoot()).thenReturn(new ResourceBase("basic"));
    // final LwM2mClient client = new LwM2mClient(server, object);
    //
    // final RegisterUplink uplink = client.startRegistration(4321, InetSocketAddress.createUnresolved("localhost",
    // 1234));
    //
    // }
    //
    // @Test(expected=IllegalArgumentException.class)
    // public void testIllegalNullAddressCreate() {
    // final ClientObject object = new ClientObject(1,
    // new SingleResourceDefinition(0, Readabl.NOT_READABLE, Writable.NOT_WRITABLE, Executable.NOT_EXECUTABLE));
    // final LwM2mClient client = new LwM2mClient(object);
    // final RegisterUplink uplink = client.startRegistration(4321, null);
    // }

}
