package leshan.client.lwm2m.response;

public interface Callback {
	public void onSuccess(OperationResponse response);
	public void onFailure(OperationResponse response);
}
