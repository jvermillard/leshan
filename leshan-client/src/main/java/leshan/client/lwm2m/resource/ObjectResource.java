package leshan.client.lwm2m.resource;

import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT;
import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CREATED;
import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvEncoder;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class ObjectResource extends ResourceBase {

	public ObjectResource(final ClientObject obj) {
		super(Integer.toString(obj.getObjectId()));
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		final Tlv[] tlvs = new Tlv[0];
		final byte[] payload = TlvEncoder.encode(tlvs).array();
		exchange.respond(CONTENT, payload);
	}

	@Override
	public void handlePOST(final CoapExchange exchange) {
		exchange.respond(CREATED, new byte[0]);
	}

}