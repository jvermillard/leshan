package leshan.client.lwm2m.operation;

import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT;
import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.METHOD_NOT_ALLOWED;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

public class ReadResponse extends BaseLwM2mResponse {

	private ReadResponse(final ResponseCode code, final byte[] payload) {
		super(code, payload);
	}

	private ReadResponse(final ResponseCode code) {
		this(code, new byte[0]);
	}

	public static ReadResponse success(final byte[] readValue) {
		return new ReadResponse(CONTENT, readValue);
	}

	public static ReadResponse failure() {
		return new ReadResponse(METHOD_NOT_ALLOWED);
	}

}
