package leshan.client.lwm2m.resource;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import leshan.client.lwm2m.operation.AggregatedLwM2mExchange;
import leshan.client.lwm2m.operation.CreateResponse;
import leshan.client.lwm2m.operation.LwM2mCreateExchange;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.LwM2mObjectInstanceCreateResponseAggregator;
import leshan.client.lwm2m.operation.LwM2mObjectInstanceReadResponseAggregator;
import leshan.client.lwm2m.operation.LwM2mResponseAggregator;
import leshan.server.lwm2m.impl.tlv.Tlv;
import leshan.server.lwm2m.impl.tlv.TlvDecoder;

public class LwM2mClientObjectInstance {

	private final LwM2mClientObjectDefinition definition;
	private final Map<Integer, LwM2mClientResource> resources;
	private final int id;

	public LwM2mClientObjectInstance(final int id, final LwM2mClientObjectDefinition definition) {
		this.id = id;
		this.resources = new HashMap<>();
		this.definition = definition;
	}

	public int getId() {
		return id;
	}

	public void handleCreate(final LwM2mCreateExchange exchange) {
		final byte[] payload = exchange.getRequestPayload();
		final Tlv[] tlvs = TlvDecoder.decode(ByteBuffer.wrap(payload));

		if (!definition.hasAllRequiredResourceIds(tlvs)) {
			exchange.respond(CreateResponse.invalidResource());
			return;
		}

		for (final LwM2mClientResourceDefinition def : definition.getResourceDefinitions()) {
			resources.put(def.getId(), def.createResource());
		}

		final LwM2mResponseAggregator aggr = new LwM2mObjectInstanceCreateResponseAggregator(exchange, tlvs.length, id);
		for (final Tlv tlv : tlvs) {
			final LwM2mClientResourceDefinition def = definition.getResourceDefinition(tlv.getIdentifier());
			if (def == null) {
				aggr.respond(tlv.getIdentifier(), CreateResponse.invalidResource());
			} else {
				final LwM2mClientResource res = def.createResource();
				final AggregatedLwM2mExchange partialExchange = new AggregatedLwM2mExchange(aggr, tlv.getIdentifier());
				partialExchange.setRequestPayload(tlv.getValue());
				resources.put(tlv.getIdentifier(), res);
				res.write(partialExchange);
			}
		}
	}

	public void handleRead(final LwM2mExchange exchange) {
		final LwM2mResponseAggregator aggr = new LwM2mObjectInstanceReadResponseAggregator(
				exchange,
				resources.size());
		for (final Entry<Integer, LwM2mClientResource> entry : resources.entrySet()) {
			final LwM2mClientResource res = entry.getValue();
			final int id = entry.getKey();
			res.read(new AggregatedLwM2mExchange(aggr, id));
		}
	}

	public void addResource(final Integer resourceId, final LwM2mClientResource resource) {
		resources.put(resourceId, resource);
	}

	public Map<Integer, LwM2mClientResource> getAllResources() {
		return new HashMap<>(resources);
	}

}
