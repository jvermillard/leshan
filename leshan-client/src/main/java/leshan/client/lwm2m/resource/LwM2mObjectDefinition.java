package leshan.client.lwm2m.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import leshan.server.lwm2m.tlv.Tlv;

public class LwM2mObjectDefinition {

	private final int id;
	private final Map<Integer, LwM2mResourceDefinition> defMap;
	private final boolean isMandatory;
	private final boolean isSingle;

	public LwM2mObjectDefinition(final int objectId, final boolean isMandatory, final boolean isSingle, final LwM2mResourceDefinition... definitions) {
		this.id = objectId;
		this.isMandatory = isMandatory;
		this.isSingle = isSingle;

		this.defMap = mapFromResourceDefinitions(definitions);
	}

	private Map<Integer, LwM2mResourceDefinition> mapFromResourceDefinitions(final LwM2mResourceDefinition[] definitions) {
		final Map<Integer, LwM2mResourceDefinition> map = new HashMap<Integer, LwM2mResourceDefinition>();
		for (final LwM2mResourceDefinition def : definitions) {
			map.put(def.getId(), def);
		}

		return map;
	}

	public int getId() {
		return id;
	}

	public boolean isMandatory() {
		return isMandatory;
	}

	public boolean isSingle()  {
		return isSingle;
	}

	public boolean hasAllRequiredResourceIds(final Tlv[] tlvs) {
		final Set<Integer> resourceIds = new HashSet<>();
		for(final Tlv tlv : tlvs) {
			resourceIds.add(tlv.getIdentifier());
		}
		for (final LwM2mResourceDefinition def : defMap.values()) {
			if (def.isRequired() && !resourceIds.contains(def.getId())) {
				return false;
			}
		}
		return true;
	}

	public LwM2mResourceDefinition getResourceDefinition(final int identifier) {
		return defMap.get(identifier);
	}

	public Collection<LwM2mResourceDefinition> getResourceDefinitions() {
		return new ArrayList<>(defMap.values());
	}

}
