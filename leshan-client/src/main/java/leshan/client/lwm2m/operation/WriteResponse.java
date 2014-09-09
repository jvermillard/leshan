package leshan.client.lwm2m.operation;

import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.BAD_REQUEST;
import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CHANGED;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

public class WriteResponse extends BaseLwM2mResponse {

	private WriteResponse(final ResponseCode code) {
		super(code, new byte[0]);
	}

	public static WriteResponse success() {
		return new WriteResponse(CHANGED);
	}

	public static WriteResponse failure() {
		return new WriteResponse(BAD_REQUEST);
	}

}
