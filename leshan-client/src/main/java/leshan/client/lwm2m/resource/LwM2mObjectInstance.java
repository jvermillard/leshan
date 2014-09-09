package leshan.client.lwm2m.resource;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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

	private final Map<Integer, LwM2mResource> resources;
	private int id;

	public static LwM2mObjectInstance instantiate(final int id, final LwM2mCreateExchange exchange,
			final Map<Integer, LwM2mResourceDefinition> definitionMap) {
		final LwM2mObjectInstance result = new LwM2mObjectInstance(id, exchange, definitionMap);
		return result.resources == null ? null : result;
	}

	private LwM2mObjectInstance(final int id, final LwM2mCreateExchange exchange,
			final Map<Integer, LwM2mResourceDefinition> definitionMap) {
		resources = new HashMap<>();
		final byte[] payload = exchange.getRequestPayload();
		final Tlv[] tlvs = TlvDecoder.decode(ByteBuffer.wrap(payload));
		final LwM2mResponseAggregator aggr = new CreateResponseAggregator(exchange, tlvs.length, id);
		for (final Tlv tlv : tlvs) {
			final LwM2mResourceDefinition def = definitionMap.get(tlv.getIdentifier());
			if (def == null) {
				aggr.respond(tlv.getIdentifier(), CreateResponse.invalidResource());
			} else {
				final LwM2mResource res = def.createResource();
				final AggregatedLwM2mExchange partialExchange = new AggregatedLwM2mExchange(aggr, tlv.getIdentifier());
				partialExchange.setRequestPayload(tlv.getValue());
				res.write(partialExchange);
				resources.put(tlv.getIdentifier(), res);
			}
		}
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
