package leshan.client.lwm2m.response;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;

public abstract class OperationResponse {

	public abstract boolean isSuccess();
	public abstract String getErrorMessage();
	public abstract ResponseCode getResponseCode();
	public abstract byte[] getPayload();
	public abstract String getLocation();

	public static OperationResponse of(final Response response) {
		return new SuccessfulOperationResponse(response);
	}

	public static OperationResponse failure(final ResponseCode responseCode, final String errorMessage) {
		return new FailedOperationResponse(responseCode, errorMessage);
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

		@Override
		public String getErrorMessage() {
			throw new UnsupportedOperationException("Successful Operations do not have Error Messages.");
		}

		@Override
		public String getLocation() {
			return response.getOptions().getLocationString();
		}
		

	}

	private static class FailedOperationResponse extends OperationResponse {
		private final ResponseCode responseCode;
		private final String errorMessage;

		public FailedOperationResponse(final ResponseCode responseCode, final String errorMessage) {
			this.responseCode = responseCode;
			this.errorMessage = errorMessage;
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
		public String getErrorMessage() {
			return errorMessage;
		}

		@Override
		public byte[] getPayload() {
			throw new UnsupportedOperationException("Failed Operations Do Not Have Payloads... for NOW...");
		}

		@Override
		public String getLocation() {
			throw new UnsupportedOperationException("Failed Operations Do Not Have Location Paths... for NOW...");
		}

	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Response[" + isSuccess() + "|" + getResponseCode() + "]");
		
		return builder.toString();
	}
	
}
