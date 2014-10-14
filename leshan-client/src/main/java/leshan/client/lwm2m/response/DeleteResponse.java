package leshan.client.lwm2m.response;

import static leshan.client.lwm2m.response.OperationResponseCode.DELETED;
import static leshan.client.lwm2m.response.OperationResponseCode.METHOD_NOT_ALLOWED;

public class DeleteResponse extends BaseLwM2mResponse {

	private DeleteResponse(final OperationResponseCode code) {
		super(code, new byte[0]);
	}

	public static DeleteResponse success() {
		return new DeleteResponse(DELETED);
	}

	public static DeleteResponse notAllowed() {
		return new DeleteResponse(METHOD_NOT_ALLOWED);
	}

}
