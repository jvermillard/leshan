package leshan.client.lwm2m;

import leshan.client.lwm2m.bootstrap.Callback;
import leshan.client.lwm2m.response.OperationResponse;

public class QuietCallback implements Callback<OperationResponse>{

	@Override
	public void onSuccess(final OperationResponse t) {
	}

	@Override
	public void onFailure(final Throwable t) {
	}
}