package leshan.client.lwm2m.operation;

import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CHANGED;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

public class ExecuteResponse extends BaseLwM2mResponse {

	private ExecuteResponse(final ResponseCode code) {
		super(code, new byte[0]);
	}

	public static ExecuteResponse success() {
		return new ExecuteResponse(CHANGED);
	}

	public static ExecuteResponse failure() {
		return new ExecuteResponse(ResponseCode.METHOD_NOT_ALLOWED);
	}

}
