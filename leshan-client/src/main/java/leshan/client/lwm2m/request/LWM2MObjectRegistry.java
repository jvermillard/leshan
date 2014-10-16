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
package leshan.client.lwm2m.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import leshan.client.lwm2m.object.LWM2MObject;
import leshan.client.lwm2m.object.LWM2MObject.Instance;

public enum LWM2MObjectRegistry {
	INSTANCE;

	private final ConcurrentHashMap<Integer, ArrayList<Instance>> createdObjects;

	private LWM2MObjectRegistry() {
		this.createdObjects = new ConcurrentHashMap<Integer, ArrayList<LWM2MObject.Instance>>();
	}

	public boolean create(final LWM2MObject.Instance instance, final Integer objectId) {
		final ArrayList<Instance> instances = new ArrayList<>();
		if(!createdObjects.containsKey(objectId)) {
			createdObjects.put(objectId, instances);
		}

		return createdObjects.get(objectId).add(instance);
	}

	public List<LWM2MObject.Instance> find(final String address) {
		final Integer objectId = objectIdFromAddress(address);
		if(createdObjects.containsKey(objectId)) {
			final List<LWM2MObject.Instance> instances = createdObjects.get(objectId);
			final Integer objectInstanceId = objectInstanceIdFromAddress(address);
			if(objectInstanceId == null) {
				return instances;
			} else {
				final Instance i = findInstanceByInstanceId(instances, objectInstanceId);
				if(i != null) {
					return Collections.singletonList(i);
				}
			}
		}

		return Collections.emptyList();
	}

	public boolean delete(final String address) {
		final Integer objectId = objectIdFromAddress(address);
		final Integer objectInstanceId = objectInstanceIdFromAddress(address);
		if(!createdObjects.containsKey(objectId) || objectInstanceId == null) {
			return false;
		}

		final List<LWM2MObject.Instance> instances = createdObjects.get(objectId);
		final LWM2MObject.Instance instance = findInstanceByInstanceId(instances, objectInstanceId);
		return createdObjects.get(objectId).remove(instance);
	}

	private static LWM2MObject.Instance findInstanceByInstanceId(final List<LWM2MObject.Instance> instances, final Integer objectInstanceId) {
		for(final Instance i : instances) {
			if(i.getInstanceId() == objectInstanceId.intValue()) {
				return i;
			}
		}

		return null;
	}

	private Integer objectIdFromAddress(final String address) {
		final String [] parts = address.split("/");
		return parts.length > 0 ? Integer.parseInt(parts[0]) : null;
	}

	private Integer objectInstanceIdFromAddress(final String address) {
		final String [] parts = address.split("/");
		return parts.length > 1 ? Integer.parseInt(parts[1]) : null;
	}
}
