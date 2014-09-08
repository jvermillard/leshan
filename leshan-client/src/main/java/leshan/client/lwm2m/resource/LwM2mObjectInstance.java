package leshan.client.lwm2m.resource;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import leshan.client.lwm2m.operation.AggregatedLwM2mExchange;
import leshan.client.lwm2m.operation.CreateResponse;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.LwM2mResourceReadResponseAggregator;
import leshan.client.lwm2m.operation.LwM2mResponseAggregator;
import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvDecoder;

public class LwM2mObjectInstance {

	private final Map<Integer, LwM2mResource> resources;
	private int id;

	private LwM2mObjectInstance(final int id, final LwM2mExchange exchange,
			final Map<Integer, LwM2mResourceDefinition> definitionMap) {

		final byte[] payload = exchange.getRequestPayload();

		final Map<Integer, LwM2mResource> convertedResources = tlvToResourceMap(definitionMap, payload);
		if (convertedResources == null) {
			resources = null;
			exchange.respond(CreateResponse.methodNotAllowed());
		} else {
			resources = new HashMap<Integer, LwM2mResource>(convertedResources);
			exchange.respond(CreateResponse.success(id));
		}
	}

	public static LwM2mObjectInstance instantiate(final int id, final LwM2mExchange exchange,
			final Map<Integer, LwM2mResourceDefinition> definitionMap) {
		final LwM2mObjectInstance result = new LwM2mObjectInstance(id, exchange, definitionMap);
		return result.resources == null ? null : result;
	}

	private Map<Integer, LwM2mResource> tlvToResourceMap(final Map<Integer, LwM2mResourceDefinition> definitionMap,
			final byte[] payload) {
		final Map<Integer, LwM2mResource> resources = new HashMap<>();
		final Tlv[] tlvs = TlvDecoder.decode(ByteBuffer.wrap(payload));
		for (final Tlv tlv : tlvs) {
			final LwM2mResourceDefinition def = definitionMap.get(tlv.getIdentifier());
			if (def == null) {
				return null;
			} else {
				resources.put(tlv.getIdentifier(), def.createResource(tlv.getValue()));
			}
		}
		return resources;
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
