package leshan.client.lwm2m.operation;

import static leshan.client.lwm2m.response.OperationResponseCode.BAD_REQUEST;
import static leshan.client.lwm2m.response.OperationResponseCode.CHANGED;
import leshan.client.lwm2m.response.OperationResponseCode;

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

}
