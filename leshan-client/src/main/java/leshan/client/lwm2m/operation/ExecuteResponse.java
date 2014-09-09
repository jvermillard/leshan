package leshan.client.lwm2m.operation;

import static leshan.client.lwm2m.response.OperationResponseCode.CHANGED;
import static leshan.client.lwm2m.response.OperationResponseCode.METHOD_NOT_ALLOWED;
import leshan.client.lwm2m.response.OperationResponseCode;

public class ExecuteResponse extends BaseLwM2mResponse {

	private ExecuteResponse(final OperationResponseCode code) {
		super(code, new byte[0]);
	}

	public static ExecuteResponse success() {
		return new ExecuteResponse(CHANGED);
	}

	// TODO Evaluate whether this needs to be used
	public static ExecuteResponse failure() {
		return new ExecuteResponse(METHOD_NOT_ALLOWED);
	}

	public static ExecuteResponse notAllowed() {
		return new ExecuteResponse(METHOD_NOT_ALLOWED);
	}

}
