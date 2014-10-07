package leshan.client.lwm2m.resource;

import java.util.concurrent.ScheduledExecutorService;

import leshan.client.lwm2m.exchange.LwM2mExchange;

public interface LwM2mClientNode {

	public void read(LwM2mExchange exchange);

	public void observe(LwM2mExchange exchange, ScheduledExecutorService service);

}
