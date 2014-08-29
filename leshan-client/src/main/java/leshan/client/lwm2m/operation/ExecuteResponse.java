package leshan.client.lwm2m.operation;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

public abstract class ExecuteResponse {

	private ExecuteResponse() {
	}

	public static ExecuteResponse success() {
		return new SuccessResponse();
	}

	public static ExecuteResponse failure() {
		return new FailureResponse();
	}

	public abstract ResponseCode getCode();

	private static class SuccessResponse extends ExecuteResponse {

		@Override
		public ResponseCode getCode() {
			return ResponseCode.CHANGED;
		}

	}

	private static class FailureResponse extends ExecuteResponse {

		@Override
		public ResponseCode getCode() {
			return ResponseCode.METHOD_NOT_ALLOWED;
		}

	}

}
