package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mExchange;

public interface LwM2mResource {

	public void read(LwM2mExchange exchange);
	public void write(LwM2mExchange exchange);
	public void execute(LwM2mExchange exchange);

	public void observe(Notifier notifier);

	public boolean isReadable();
	public void notifyResourceUpdated();
}
