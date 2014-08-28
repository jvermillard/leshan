package leshan.client.lwm2m.resource;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

public abstract class WriteResponse {

	private WriteResponse() {
	}

	public static WriteResponse success() {
		return new SuccessResponse();
	}

	public static WriteResponse failure() {
		return new FailureResponse();
	}

	public abstract ResponseCode getCode();

	private static class SuccessResponse extends WriteResponse {

		@Override
		public ResponseCode getCode() {
			return ResponseCode.CHANGED;
		}

	}

	private static class FailureResponse extends WriteResponse {

		@Override
		public ResponseCode getCode() {
			return ResponseCode.BAD_REQUEST;
		}

	}

}
