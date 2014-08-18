package leshan.client.lwm2m.bootstrap;

public interface Callback<T> {
	public void onSuccess(T t);
	public void onFailure(Throwable t);
}
