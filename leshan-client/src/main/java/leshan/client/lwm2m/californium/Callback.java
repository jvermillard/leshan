package leshan.client.lwm2m.californium;

public interface Callback<T> {

	public void onSuccess(T t);

	public void onFailure();

}
