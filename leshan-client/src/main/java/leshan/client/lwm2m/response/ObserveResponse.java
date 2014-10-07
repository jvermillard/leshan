package leshan.client.lwm2m.response;

import static leshan.client.lwm2m.response.OperationResponseCode.CHANGED;

public class ObserveResponse extends BaseLwM2mResponse {

	private ObserveResponse(final OperationResponseCode code, final byte[] payload) {
		super(code, payload);
	}

	public static ObserveResponse notifyWithContent(final byte[] payload) {
		return new ObserveResponse(CHANGED, payload);
	}

}
