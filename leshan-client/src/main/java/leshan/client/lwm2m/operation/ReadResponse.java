package leshan.client.lwm2m.operation;

import java.util.Arrays;
import java.util.Objects;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

public abstract class ReadResponse implements LwM2mResponse {

	private ReadResponse() {
	}

	public static ReadResponse success(final byte[] readValue) {
		return new SuccessResponse(readValue);
	}

	public static ReadResponse failure() {
		return new FailureResponse();
	}

	private static class SuccessResponse extends ReadResponse {

		private final byte[] value;

		public SuccessResponse(final byte[] readValue) {
			this.value = readValue;
		}

		@Override
		public ResponseCode getCode() {
			return ResponseCode.CONTENT;
		}

		@Override
		public byte[] getResponsePayload() {
			return value;
		}

		@Override
		public boolean equals(final Object o) {
			if (!(o instanceof SuccessResponse)) {
				return false;
			}
			final SuccessResponse other = (SuccessResponse)o;
			return Arrays.equals(value, other.value);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(value);
		}

	}

	private static class FailureResponse extends ReadResponse {

		@Override
		public ResponseCode getCode() {
			return ResponseCode.METHOD_NOT_ALLOWED;
		}

		@Override
		public byte[] getResponsePayload() {
			return new byte[0];
			//			throw new IllegalAccessError("Failure Reads Do Not Have Values");
		}

		@Override
		public boolean equals(final Object o) {
			return o instanceof FailureResponse;
		}

		@Override
		public int hashCode() {
			return Objects.hash();
		}

	}

}
