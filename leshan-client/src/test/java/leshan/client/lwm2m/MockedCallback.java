package leshan.client.lwm2m;

import java.util.concurrent.atomic.AtomicBoolean;

import ch.ethz.inf.vs.californium.coap.Message;
import leshan.client.lwm2m.response.OperationResponse;

public class MockedCallback implements Callback {
	

	private final AtomicBoolean called;
	private OperationResponse response;
	
	public MockedCallback() {
		called = new AtomicBoolean(false);
	}

	@Override
	public void onSuccess(final OperationResponse t) {
		called.set(true);
		response = t;
	}

	@Override
	public void onFailure(final OperationResponse t) {
		called.set(true);
		response = t;
	}

	public AtomicBoolean isCalled() {
		return called;
	}

	public byte[] getResponsePayload() {
		return response.getPayload();
	}
}