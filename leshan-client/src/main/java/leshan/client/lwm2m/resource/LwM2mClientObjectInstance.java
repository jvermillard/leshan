package leshan.client.lwm2m.resource;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;

import leshan.client.lwm2m.exchange.LwM2mCreateExchange;
import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.exchange.ObserveNotifyExchange;
import leshan.client.lwm2m.exchange.aggregate.AggregatedLwM2mExchange;
import leshan.client.lwm2m.exchange.aggregate.LwM2mObjectInstanceCreateResponseAggregator;
import leshan.client.lwm2m.exchange.aggregate.LwM2mObjectInstanceReadResponseAggregator;
import leshan.client.lwm2m.exchange.aggregate.LwM2mResponseAggregator;
import leshan.client.lwm2m.response.CreateResponse;
import leshan.client.lwm2m.response.WriteResponse;
import leshan.server.lwm2m.impl.tlv.Tlv;
import leshan.server.lwm2m.impl.tlv.TlvDecoder;
import leshan.server.lwm2m.observation.ObserveSpec;

public class LwM2mClientObjectInstance implements LwM2mClientNode {

	private final LwM2mClientObjectDefinition definition;
	private final Map<Integer, LwM2mClientResource> resources;
	private final int id;
	private ObserveSpec observeSpec;

	public LwM2mClientObjectInstance(final int id, final LwM2mClientObjectDefinition definition) {
		this.id = id;
		this.resources = new HashMap<>();
		this.definition = definition;
		this.observeSpec = new ObserveSpec.Builder().build();
	}

	public int getId() {
		return id;
	}

	public void createMandatory() {
		for (final LwM2mClientResourceDefinition def : definition.getResourceDefinitions()) {
			resources.put(def.getId(), def.createResource());
		}
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

	public void read(final LwM2mExchange exchange) {
		final LwM2mResponseAggregator aggr = new LwM2mObjectInstanceReadResponseAggregator(
				exchange,
				resources.size());
		for (final Entry<Integer, LwM2mClientResource> entry : resources.entrySet()) {
			final LwM2mClientResource res = entry.getValue();
			final int id = entry.getKey();
			res.read(new AggregatedLwM2mExchange(aggr, id));
		}
	}

	@Override
	public void observe(LwM2mExchange exchange, ScheduledExecutorService service) {
		new ObserveNotifyExchange(exchange, this, observeSpec, service);
	}

	public void writeAttributes(LwM2mExchange exchange, ObserveSpec spec) {
		this.observeSpec = spec;
		exchange.respond(WriteResponse.success());
	}

	public void addResource(final Integer resourceId, final LwM2mClientResource resource) {
		resources.put(resourceId, resource);
	}

	public Map<Integer, LwM2mClientResource> getAllResources() {
		return new HashMap<>(resources);
	}

}
