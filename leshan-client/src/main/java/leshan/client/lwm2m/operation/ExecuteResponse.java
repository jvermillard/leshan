package leshan.client.lwm2m.operation;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

public abstract class ExecuteResponse implements LwM2mResponse {

	private ExecuteResponse() {
	}

	public static ExecuteResponse success() {
		return new SuccessResponse();
	}

	public static ExecuteResponse failure() {
		return new FailureResponse();
	}

	private static class SuccessResponse extends ExecuteResponse {

		@Override
		public ResponseCode getCode() {
			return ResponseCode.CHANGED;
		}

		@Override
		public byte[] getResponsePayload() {
			return new byte[0];
		}

	}

	private static class FailureResponse extends ExecuteResponse {

		@Override
		public ResponseCode getCode() {
			return ResponseCode.METHOD_NOT_ALLOWED;
		}

		@Override
		public byte[] getResponsePayload() {
			return new byte[0];
		}

	}

}
