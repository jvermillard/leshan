package leshan.client.lwm2m.resource;

import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.BAD_REQUEST;
import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT;
import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CREATED;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvDecoder;
import leshan.server.lwm2m.tlv.TlvEncoder;
import leshan.server.lwm2m.tlv.TlvType;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class ObjectResource extends ResourceBase {

	private final ClientObject obj;

	public ObjectResource(final ClientObject obj) {
		super(Integer.toString(obj.getObjectId()));
		this.obj = obj;
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		final List<Tlv> tlvs = new ArrayList<>();

		for (final Resource res : getChildren()) {
			final ClientObjectInstance instance = (ClientObjectInstance)res;
			final List<Tlv> resources = new ArrayList<>();
			for (final ClientResource resource : instance.getResources().values()) {
				resources.add(new Tlv(TlvType.RESOURCE_VALUE, null, resource.getValue(), resource.getId()));
			}
			tlvs.add(new Tlv(TlvType.OBJECT_INSTANCE, resources.toArray(new Tlv[0]), null, instance.getInstanceId()));
		}

		final byte[] payload = TlvEncoder.encode(tlvs.toArray(new Tlv[0])).array();
		exchange.respond(CONTENT, payload);
	}

	@Override
	public void handlePOST(final CoapExchange exchange) {
		final Tlv[] tlvs = TlvDecoder.decode(ByteBuffer.wrap(exchange.getRequestPayload()));
		final Map<Integer, ClientResource> resources = new TreeMap<>();
		for (final Tlv tlv : tlvs) {
			if (tlv.getType() != TlvType.RESOURCE_VALUE) {
				exchange.respond(BAD_REQUEST, "Invalid Object Instance TLV");
			}
			resources.put(tlv.getIdentifier(), new ClientResource(tlv.getIdentifier(), tlv.getValue()));
		}
		final ClientObjectInstance instance = new ClientObjectInstance(resources);
		this.add(instance);
		exchange.respond(CREATED, "/" + obj.getObjectId() + "/0");
	}

}

class ClientObjectInstance extends ResourceBase {

	private static final int INSTANCE_ID = 0;
	private final Map<Integer, ClientResource> resources;

	public ClientObjectInstance(final Map<Integer, ClientResource> resources) {
		super(Integer.toString(INSTANCE_ID));
		this.resources = resources;
	}

	public int getInstanceId() {
		return INSTANCE_ID;
	}

	public Map<Integer, ClientResource> getResources() {
		return resources;
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		final List<Tlv> tlvs = new ArrayList<>();
		for (final ClientResource resource : resources.values()) {
			tlvs.add(new Tlv(TlvType.RESOURCE_VALUE, null, resource.getValue(), resource.getId()));
		}
		Tlv[] tlvArray = tlvs.toArray(new Tlv[0]);
		exchange.respond(CONTENT, TlvEncoder.encode(tlvArray).array());
	}

}

class ClientResource {

	private final int id;
	private final byte[] value;


	public ClientResource(final int id, final byte[] value) {
		this.id = id;
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public byte[] getValue() {
		return value;
	}

}
