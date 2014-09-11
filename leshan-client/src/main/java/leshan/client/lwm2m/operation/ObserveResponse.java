package leshan.client.lwm2m.operation;

import static leshan.client.lwm2m.response.OperationResponseCode.CHANGED;
import leshan.client.lwm2m.response.OperationResponseCode;

public class ObserveResponse extends BaseLwM2mResponse {

	private ObserveResponse(final OperationResponseCode code, final byte[] payload) {
		super(code, payload);
	}

	public static ObserveResponse notifyWithContent(final byte[] payload) {
		return new ObserveResponse(CHANGED, payload);
	}

}
