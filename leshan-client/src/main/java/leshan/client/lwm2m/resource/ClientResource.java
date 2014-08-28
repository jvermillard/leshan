package leshan.client.lwm2m.resource;

import java.util.Arrays;

import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvType;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

class ClientResource extends ResourceBase {

	private byte[] value;

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

	@Override
	public void handleGET(final CoapExchange exchange) {
		exchange.respond(ResponseCode.CONTENT, value);
	}

	@Override
	public void handlePUT(final CoapExchange exchange) {
		final byte[] payload = exchange.getRequestPayload();
		value = Arrays.copyOf(payload, payload.length);
		exchange.respond(ResponseCode.CHANGED);
	}

}