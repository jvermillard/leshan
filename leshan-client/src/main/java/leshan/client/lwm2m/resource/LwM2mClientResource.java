package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.exchange.LwM2mExchange;

public abstract class LwM2mClientResource extends LwM2mClientNode {

	@Override
	public abstract void read(LwM2mExchange exchange);

	public abstract void write(LwM2mExchange exchange);

	public abstract void execute(LwM2mExchange exchange);

	public abstract boolean isReadable();

	public abstract void notifyResourceUpdated();
}
