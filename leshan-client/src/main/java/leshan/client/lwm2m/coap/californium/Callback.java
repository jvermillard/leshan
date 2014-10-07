package leshan.client.lwm2m.coap.californium;

public interface Callback<T> {

	public void onSuccess(T t);

	public void onFailure();

}
