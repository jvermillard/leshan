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
