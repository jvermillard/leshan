package leshan.client.lwm2m.operation;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

public abstract class WriteResponse implements LwM2mResponse {

	private WriteResponse() {
	}

	public static WriteResponse success() {
		return new SuccessResponse();
	}

	public static WriteResponse failure() {
		return new FailureResponse();
	}

	private static class SuccessResponse extends WriteResponse {

		@Override
		public ResponseCode getCode() {
			return ResponseCode.CHANGED;
		}

		@Override
		public byte[] getResponsePayload() {
			return new byte[0];
		}

	}

	private static class FailureResponse extends WriteResponse {

		@Override
		public ResponseCode getCode() {
			return ResponseCode.BAD_REQUEST;
		}

		@Override
		public byte[] getResponsePayload() {
			return new byte[0];
		}

	}

}
