package leshan.client.lwm2m.operation;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

public abstract class ReadResponse {

	private ReadResponse() {
	}


	public static ReadResponse success(final ResponseCode code, final byte[] readValue) {
		return new SuccessResponse(code, readValue);
	}
	
	public static ReadResponse success(final byte[] readValue) {
		return new SuccessResponse(ResponseCode.CONTENT, readValue);
	}

	public static ReadResponse failure() {
		return new FailureResponse();
	}
	
	public abstract byte[] getValue();

	public abstract ResponseCode getCode();

	private static class SuccessResponse extends ReadResponse {

		private final byte[] value;
		private final ResponseCode code;

		public SuccessResponse(final ResponseCode code, final byte[] readValue) {
			this.code = code;
			this.value = readValue;
		}

		@Override
		public ResponseCode getCode() {
			return code;
		}
		
		@Override
		public byte[] getValue() {
			return value;
		}

	}

	private static class FailureResponse extends ReadResponse {

		@Override
		public ResponseCode getCode() {
			return ResponseCode.METHOD_NOT_ALLOWED;
		}

		@Override
		public byte[] getValue() {
			throw new IllegalAccessError("Failure Reads Do Not Have Values");
		}

	}

}
