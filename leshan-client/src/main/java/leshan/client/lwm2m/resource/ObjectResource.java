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

public class ObjectResource extends ResourceBase {

	private final ClientObject obj;
	private final AtomicInteger instanceCounter;

	public ObjectResource(final ClientObject obj) {
		super(Integer.toString(obj.getObjectId()));
		this.obj = obj;
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
		for (final Tlv tlv : tlvs) {
			if (tlv.getType() != TlvType.RESOURCE_VALUE) {
				exchange.respond(BAD_REQUEST, "Invalid Object Instance TLV");
			}
			resources.put(tlv.getIdentifier(), new ClientResource(tlv.getIdentifier(), tlv.getValue()));
		}
		final ClientObjectInstance instance = new ClientObjectInstance(instanceCounter.getAndIncrement(), resources);
		this.add(instance);
		exchange.respond(CREATED, "/" + obj.getObjectId() + "/" + instance.getInstanceId());
	}

}

class ClientObjectInstance extends ResourceBase {

	public ClientObjectInstance(final int instanceID, final Map<Integer, ClientResource> resources) {
		super(Integer.toString(instanceID));
		for(final Map.Entry<Integer, ClientResource> entry : resources.entrySet()){
			add(entry.getValue());
		}
	}

	public int getInstanceId() {
		return Integer.parseInt(getName());
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		final Tlv[] tlvArray = asTlvArray();
		exchange.respond(CONTENT, TlvEncoder.encode(tlvArray).array());
	}

	public Tlv[] asTlvArray() {
		final List<Tlv> tlvs = new ArrayList<>();
		for (final Resource res : getChildren()) {
			tlvs.add(((ClientResource) res).asTlv());
		}
		final Tlv[] tlvArray = tlvs.toArray(new Tlv[0]);
		return tlvArray;
	}

}

class ClientResource extends ResourceBase{

	private final byte[] value;


	public ClientResource(final int id, final byte[] value) {
		super(Integer.toString(id));
		this.value = value;
	}

	public int getId() {
		return Integer.parseInt(getName());
	}

	public byte[] getValue() {
		return value;
	}

	public Tlv asTlv() {
		return new Tlv(TlvType.RESOURCE_VALUE, null, getValue(), getId());
	}

}
