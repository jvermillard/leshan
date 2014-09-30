package leshan.client.lwm2m.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import leshan.server.lwm2m.impl.tlv.Tlv;

public class LwM2mClientObjectDefinition {

	private final int id;
	private final Map<Integer, LwM2mClientResourceDefinition> defMap;
	private final boolean isMandatory;
	private final boolean isSingle;

	public LwM2mClientObjectDefinition(final int objectId, final boolean isMandatory, final boolean isSingle, final LwM2mClientResourceDefinition... definitions) {
		this.id = objectId;
		this.isMandatory = isMandatory;
		this.isSingle = isSingle;

		this.defMap = mapFromResourceDefinitions(definitions);
	}

	private Map<Integer, LwM2mClientResourceDefinition> mapFromResourceDefinitions(final LwM2mClientResourceDefinition[] definitions) {
		final Map<Integer, LwM2mClientResourceDefinition> map = new HashMap<Integer, LwM2mClientResourceDefinition>();
		for (final LwM2mClientResourceDefinition def : definitions) {
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
		for (final LwM2mClientResourceDefinition def : defMap.values()) {
			if (def.isRequired() && !resourceIds.contains(def.getId())) {
				return false;
			}
		}
		return true;
	}

	public LwM2mClientResourceDefinition getResourceDefinition(final int identifier) {
		return defMap.get(identifier);
	}

	public Collection<LwM2mClientResourceDefinition> getResourceDefinitions() {
		return new ArrayList<>(defMap.values());
	}
	
}
