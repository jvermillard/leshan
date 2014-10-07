package leshan.client.lwm2m.response;

import static leshan.client.lwm2m.response.OperationResponseCode.BAD_REQUEST;
import static leshan.client.lwm2m.response.OperationResponseCode.CHANGED;
import static leshan.client.lwm2m.response.OperationResponseCode.METHOD_NOT_ALLOWED;

public class WriteResponse extends BaseLwM2mResponse {

	private WriteResponse(final OperationResponseCode code) {
		super(code, new byte[0]);
	}

	public static WriteResponse success() {
		return new WriteResponse(CHANGED);
	}

	public static WriteResponse failure() {
		return new WriteResponse(BAD_REQUEST);
	}

	public static WriteResponse notAllowed() {
		return new WriteResponse(METHOD_NOT_ALLOWED);
	}

	public static WriteResponse badRequest() {
		return new WriteResponse(BAD_REQUEST);
	}

}
