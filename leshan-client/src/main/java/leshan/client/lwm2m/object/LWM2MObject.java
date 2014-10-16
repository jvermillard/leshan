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
package leshan.client.lwm2m.object;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.californium.core.coap.Request;
import org.eclipse.jetty.util.ConcurrentHashSet;

public final class LWM2MObject {

	private final Set<Instance> createdObjects;

	private LWM2MObject(final InputStream stream) {
		this.createdObjects = new ConcurrentHashSet<>();

	}

	public static List<LWM2MObject> createObjectModel(final InputStream stream) {
		return Collections.singletonList(new LWM2MObject(stream));
	}

	public static List<LWM2MObject> createObjectModel(final File file) throws FileNotFoundException {
		return Collections.singletonList(new LWM2MObject(new FileInputStream(file)));
	}

	public static List<LWM2MObject> createObjectModel(final String schema) {
		return Collections.singletonList(new LWM2MObject(new ByteArrayInputStream(schema.getBytes())));
	}

	public LWM2MObject.Instance createInstance(final LWM2MServer server, final LWM2MObject obj, final int objectInstanceId) {


		final Instance instance = new Instance(server, obj, objectInstanceId);
		// TODO: Check if instance has already been created
		if(this.createdObjects.contains(instance)) {

		}
		this.createdObjects.add(instance);

		return instance;
	}

	public final class Instance {
		private final int instanceId;

		protected Instance(final LWM2MServer server, final LWM2MObject obj, final int instanceId) {
			this.instanceId = instanceId;
		}

		public boolean isOperationPermitted(final LWM2MServer server, final Request request) {
			return false;
		}

		public int getInstanceId() {
			return instanceId;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(instanceId);
		}

		@Override
		public boolean equals(final Object o) {
			if(this == o) { return true; }
			if(!(o instanceof Instance)) { return false; }

			final Instance other = (Instance) o;
			return Objects.equals(this.instanceId, other.instanceId);
		}
	}

}
