package leshan.client.lwm2m.util;

import java.util.concurrent.atomic.AtomicBoolean;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import leshan.client.lwm2m.response.Callback;
import leshan.client.lwm2m.response.OperationResponse;

public class ResponseCallback implements Callback {
	

	private final AtomicBoolean called;
	private OperationResponse response;
	
	public ResponseCallback() {
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
	
	public ResponseCode getResponseCode(){
		return response.getResponseCode();
	}
	
	public boolean isSuccess(){
		return response.isSuccess();
	}

	public void reset() {
		called.set(false);
	}

	public OperationResponse getResponse() {
		return response;
	}
}