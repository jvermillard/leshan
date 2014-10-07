package leshan.client.lwm2m.resource;

import java.util.concurrent.ScheduledExecutorService;

import leshan.client.lwm2m.exchange.LwM2mExchange;

public interface LwM2mClientResource {

	public void read(LwM2mExchange exchange);
	public void write(LwM2mExchange exchange);
	public void execute(LwM2mExchange exchange);
	public void observe(LwM2mExchange exchange, ScheduledExecutorService service);

	public boolean isReadable();
	public void notifyResourceUpdated();
}
