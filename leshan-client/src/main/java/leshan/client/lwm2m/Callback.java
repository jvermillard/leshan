package leshan.client.lwm2m;

public interface Callback<T> {
	public void onSuccess(T t);
	public void onFailure(Throwable t);
}
