package leshan.client.lwm2m;

import leshan.client.lwm2m.response.OperationResponse;

public interface Callback {
	public void onSuccess(OperationResponse response);
	public void onFailure(OperationResponse response);
}
