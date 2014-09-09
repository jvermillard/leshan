package leshan.client.lwm2m.resource;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import leshan.client.lwm2m.operation.AggregatedLwM2mExchange;
import leshan.client.lwm2m.operation.CreateResponse;
import leshan.client.lwm2m.operation.LwM2mObjectInstanceCreateResponseAggregator;
import leshan.client.lwm2m.operation.LwM2mCreateExchange;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.LwM2mObjectInstanceReadResponseAggregator;
import leshan.client.lwm2m.operation.LwM2mResponseAggregator;
import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvDecoder;

public class LwM2mObjectInstance {

	private final LwM2mObjectDefinition definition;
	private final Map<Integer, LwM2mResource> resources;
	private final int id;

	public LwM2mObjectInstance(final int id, final LwM2mObjectDefinition definition) {
		this.id = id;
		this.resources = new HashMap<>();
		this.definition = definition;
	}

	public void handleCreate(final LwM2mCreateExchange exchange) {
		final byte[] payload = exchange.getRequestPayload();
		final Tlv[] tlvs = TlvDecoder.decode(ByteBuffer.wrap(payload));

		if (!definition.hasAllRequiredResourceIds(tlvs)) {
			exchange.respond(CreateResponse.invalidResource());
			return;
		}

		final Collection<LwM2mResourceDefinition> auto = definition.getNonWritableResourceDefinitions();
		for (final LwM2mResourceDefinition def : auto) {
			resources.put(def.getId(), def.createResource());
		}

		final LwM2mResponseAggregator aggr = new LwM2mObjectInstanceCreateResponseAggregator(exchange, tlvs.length, id);
		for (final Tlv tlv : tlvs) {
			final LwM2mResourceDefinition def = definition.getResourceDefinition(tlv.getIdentifier());
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

	public int getId() {
		return id;
	}

	public void handleNormalRead(final LwM2mExchange exchange) {
		final LwM2mResponseAggregator aggr = new LwM2mObjectInstanceReadResponseAggregator(
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
