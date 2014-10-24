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
package leshan.server.impl;

import java.io.IOException;

import leshan.server.client.Client;
import leshan.server.impl.ObservationRegistryImpl;
import leshan.server.node.LwM2mPath;
import leshan.server.observation.Observation;
import leshan.server.observation.ObservationListener;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ObservationRegistryImplTest extends BasicTestSupport {

    ObservationRegistryImpl registry;

    @Before
    public void setUp() throws Exception {
        registry = new ObservationRegistryImpl();
    }

    @Test
    public void add_duplicate_observation() throws IOException {
        givenASimpleClient();

        Observation obs = new ObservationImpl(client, new LwM2mPath(3, 0, 15));
        registry.addObservation(obs);

        Observation duplicate = new ObservationImpl(client, new LwM2mPath(3, 0, 15));

        registry.addObservation(duplicate);
        Assert.assertSame(1, registry.cancelObservations(client));
    }

    private class ObservationImpl implements Observation {

        private Client client;
        private LwM2mPath path;

        public ObservationImpl(Client client, LwM2mPath path) {
            this.client = client;
            this.path = path;
        }

        @Override
        public Client getClient() {
            return client;
        }

        @Override
        public LwM2mPath getPath() {
            return path;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void addListener(ObservationListener listener) {
        }

        @Override
        public void removeListener(ObservationListener listener) {
        }
    }
}
