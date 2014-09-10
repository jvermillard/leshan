package leshan.client.lwm2m.operation;

import static leshan.client.lwm2m.response.OperationResponseCode.CONTENT;
import static leshan.client.lwm2m.response.OperationResponseCode.METHOD_NOT_ALLOWED;
import leshan.client.lwm2m.response.OperationResponseCode;

public class ReadResponse extends BaseLwM2mResponse {

	private ReadResponse(final OperationResponseCode code, final byte[] payload) {
		super(code, payload);
	}

	private ReadResponse(final OperationResponseCode code) {
		this(code, new byte[0]);
	}

	public static ReadResponse success(final byte[] readValue) {
		return new ReadResponse(CONTENT, readValue);
	}

	// TODO Evaluate whether this needs to be used
	public static ReadResponse failure() {
		return new ReadResponse(METHOD_NOT_ALLOWED);
	}

	public static ReadResponse notAllowed() {
		return new ReadResponse(METHOD_NOT_ALLOWED);
	}

}
