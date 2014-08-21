package leshan.client.lwm2m.response;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Response;

public abstract class OperationResponse {

	public abstract boolean isSuccess();
	public abstract ResponseCode getResponseCode();
	public abstract byte[] getPayload();

	public static OperationResponse of(final Response response) {
		return new SuccessfulOperationResponse(response);
	}

	public static OperationResponse failure(final ResponseCode responseCode) {
		return new FailedOperationResponse(responseCode);
	}

	private static class SuccessfulOperationResponse extends OperationResponse {
		private final Response response;

		public SuccessfulOperationResponse(final Response response) {
			this.response = response;
		}

		@Override
		public boolean isSuccess() {
			return true;
		}

		@Override
		public ResponseCode getResponseCode() {
			return response.getCode();
		}

		@Override
		public byte[] getPayload() {
			return response.getPayload();
		}

	}

	private static class FailedOperationResponse extends OperationResponse {
		private final ResponseCode responseCode;

		public FailedOperationResponse(final ResponseCode responseCode) {
			this.responseCode = responseCode;
		}

		@Override
		public boolean isSuccess() {
			return false;
		}

		@Override
		public ResponseCode getResponseCode() {
			return responseCode;
		}

		@Override
		public byte[] getPayload() {
			throw new UnsupportedOperationException("Failed Operations Do Not Have Payloads... for NOW...");
		}

	}

}
