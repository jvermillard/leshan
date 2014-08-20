package leshan.client.lwm2m.response;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Response;

public abstract class OperationResponse {

	public abstract boolean isSuccess();
	public abstract ResponseCode getResponseCode();

	public static OperationResponse of(final ResponseCode responseCode) {
		return new SuccessfulOperationResponse(responseCode);
	}

	public static OperationResponse failure(final ResponseCode responseCode) {
		return new FailedOperationResponse(responseCode);
	}

	private static class SuccessfulOperationResponse extends OperationResponse {
		private final Response response;

		public SuccessfulOperationResponse(final ResponseCode responseCode) {
			this.response = new Response(responseCode);
		}

		@Override
		public boolean isSuccess() {
			return true;
		}

		@Override
		public ResponseCode getResponseCode() {
			return response.getCode();
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

	}
}
