package leshan.client.lwm2m.resource;

import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.BAD_REQUEST;
import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT;
import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CREATED;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvDecoder;
import leshan.server.lwm2m.tlv.TlvEncoder;
import leshan.server.lwm2m.tlv.TlvType;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class ClientObject extends ResourceBase {

	private final ClientResourceDefinition[] definitions;
	private final AtomicInteger instanceCounter;

	public ClientObject(final int objectId, final ClientResourceDefinition... definitions) {
		super(Integer.toString(objectId));
		if (definitions == null || definitions.length == 0) {
			throw new IllegalArgumentException("Must provide at least one resource definition");
		}
		this.definitions = definitions;
		instanceCounter = new AtomicInteger(0);
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		final List<Tlv> tlvs = new ArrayList<>();

		for (final Resource res : getChildren()) {
			final ClientObjectInstance instance = (ClientObjectInstance)res;
			tlvs.add(new Tlv(TlvType.OBJECT_INSTANCE, instance.asTlvArray(), null, instance.getInstanceId()));
		}

		final byte[] payload = TlvEncoder.encode(tlvs.toArray(new Tlv[0])).array();
		exchange.respond(CONTENT, payload);
	}

	@Override
	public void handlePOST(final CoapExchange exchange) {
		final Tlv[] tlvs = TlvDecoder.decode(ByteBuffer.wrap(exchange.getRequestPayload()));
		final Map<Integer, ClientResource> resources = new TreeMap<>();
		for (final ClientResourceDefinition def : definitions) {
			resources.put(def.getId(), def.createResource());
		}
		
		final ClientObjectInstance instance = new ClientObjectInstance(instanceCounter.getAndIncrement(), resources);
		this.add(instance);

		for (final Tlv tlv : tlvs) {
			if (tlv.getType() != TlvType.RESOURCE_VALUE) {
				exchange.respond(BAD_REQUEST, "Invalid Object Instance TLV");
			}
			resources.get(tlv.getIdentifier()).writeTlv(tlv);
		}
		exchange.respond(CREATED, "/" + getName() + "/" + instance.getInstanceId());
	}

}
