package leshan.client.lwm2m.resource;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import leshan.client.lwm2m.operation.AggregatedLwM2mExchange;
import leshan.client.lwm2m.operation.CreateResponse;
import leshan.client.lwm2m.operation.CreateResponseAggregator;
import leshan.client.lwm2m.operation.LwM2mCreateExchange;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.LwM2mResourceReadResponseAggregator;
import leshan.client.lwm2m.operation.LwM2mResponseAggregator;
import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvDecoder;

public class LwM2mObjectInstance {

	private final Map<Integer, LwM2mResourceDefinition> definitionMap;
	private final Map<Integer, LwM2mResource> resources;
	private final int id;

	public LwM2mObjectInstance(final int id, final Map<Integer, LwM2mResourceDefinition> definitionMap) {
		this.id = id;
		this.resources = new HashMap<>();
		this.definitionMap = definitionMap;
	}

	public void handleCreate(final LwM2mCreateExchange exchange) {
		final byte[] payload = exchange.getRequestPayload();
		final Tlv[] tlvs = TlvDecoder.decode(ByteBuffer.wrap(payload));

		if (!hasAllRequiredKeys(definitionMap, tlvs)) {
			exchange.respond(CreateResponse.invalidResource());
			return;
		}

		final LwM2mResponseAggregator aggr = new CreateResponseAggregator(exchange, tlvs.length, id);
		for (final Tlv tlv : tlvs) {
			final LwM2mResourceDefinition def = definitionMap.get(tlv.getIdentifier());
			if (def == null) {
				aggr.respond(tlv.getIdentifier(), CreateResponse.invalidResource());
			} else {
				final LwM2mResource res = def.createResource();
				final AggregatedLwM2mExchange partialExchange = new AggregatedLwM2mExchange(aggr, tlv.getIdentifier());
				partialExchange.setRequestPayload(tlv.getValue());
				resources.put(tlv.getIdentifier(), res);
				res.write(partialExchange);
			}
		}
	}

	private boolean hasAllRequiredKeys(final Map<Integer, LwM2mResourceDefinition> definitionMap,
			final Tlv[] tlvs) {
		final Set<Integer> resourceIds = new HashSet<>();
		for(final Tlv tlv : tlvs) {
			resourceIds.add(tlv.getIdentifier());
		}
		final Set<Entry<Integer, LwM2mResourceDefinition>> entrySet = definitionMap.entrySet();
		for (final Entry<Integer, LwM2mResourceDefinition> entry : entrySet) {
			if (entry.getValue().isRequired() && !resourceIds.contains(entry.getKey())) {
				return false;
			}
		}
		return true;
	}

	public int getId() {
		return id;
	}

	public void handleNormalRead(final LwM2mExchange exchange) {
		final LwM2mResponseAggregator aggr = new LwM2mResourceReadResponseAggregator(
				exchange,
				resources.size());
		for (final Entry<Integer, LwM2mResource> entry : resources.entrySet()) {
			final LwM2mResource res = entry.getValue();
			final int id = entry.getKey();
			res.read(new AggregatedLwM2mExchange(aggr, id));
		}
	}

	public void addResource(final Integer resourceId, final LwM2mResource resource) {
		resources.put(resourceId, resource);
	}

	public Map<Integer, LwM2mResource> getAllResources() {
		return new HashMap<>(resources);
	}

}
