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
package leshan.server.lwm2m.impl;

import java.io.IOException;

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.node.LwM2mPath;
import leshan.server.lwm2m.observation.Observation;
import leshan.server.lwm2m.observation.ObservationListener;

import org.eclipse.californium.core.coap.Request;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ObservationRegistryImplTest extends BasicTestSupport {

    ObservationRegistryImpl registry;

    @Before
    public void setUp() throws Exception {
        this.registry = new ObservationRegistryImpl();
    }

    @Test
    public void testAddObservationHandlesDuplicateRegistration() throws IOException {
        givenASimpleClient();

        final Observation obs = new TestObservation(Request.newGet(), this.client, new LwM2mPath(3, 0, 15));
        registry.addObservation(obs);

        final Observation duplicate = new TestObservation(Request.newGet(), this.client, new LwM2mPath(3, 0, 15));

        registry.addObservation(duplicate);
        Assert.assertSame(1, this.registry.cancelObservations(this.client));
        
       
    }
    
    class TestObservation implements Observation {
		
		private final Request request;
		private final Client client;
		private final LwM2mPath lwM2mPath;

		public TestObservation(final Request request, final Client client,
				final LwM2mPath lwM2mPath) {
			this.request = request;
			this.client = client;
			this.lwM2mPath = lwM2mPath;
		}

		@Override
		public void removeListener(final ObservationListener listener) {
		}
		
		@Override
		public LwM2mPath getPath() {
			return lwM2mPath;
		}
		
		@Override
		public Client getClient() {
			return client;
		}
		
		@Override
		public void cancel() {
		}
		
		@Override
		public void addListener(final ObservationListener listener) {
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((client == null) ? 0 : client.hashCode());
			result = prime * result
					+ ((lwM2mPath == null) ? 0 : lwM2mPath.hashCode());
			result = prime * result
					+ ((request == null) ? 0 : request.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final TestObservation other = (TestObservation) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (client == null) {
				if (other.client != null)
					return false;
			} else if (!client.equals(other.client))
				return false;
			if (lwM2mPath == null) {
				if (other.lwM2mPath != null)
					return false;
			} else if (!lwM2mPath.equals(other.lwM2mPath))
				return false;
			if (request == null) {
				if (other.request != null)
					return false;
			} else if (!request.equals(other.request))
				return false;
			return true;
		}

		private ObservationRegistryImplTest getOuterType() {
			return ObservationRegistryImplTest.this;
		}
	};
}
