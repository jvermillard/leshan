package leshan.client.lwm2m.resource;

import java.util.concurrent.ScheduledExecutorService;

import leshan.client.lwm2m.exchange.LwM2mExchange;

public abstract class LwM2mClientResource extends LwM2mClientNode {

	@Override
	public abstract void read(LwM2mExchange exchange);

	@Override
	public abstract void observe(LwM2mExchange exchange, ScheduledExecutorService service);

	public abstract void write(LwM2mExchange exchange);

	public abstract void execute(LwM2mExchange exchange);

	public abstract boolean isReadable();

	public abstract void notifyResourceUpdated();
}
