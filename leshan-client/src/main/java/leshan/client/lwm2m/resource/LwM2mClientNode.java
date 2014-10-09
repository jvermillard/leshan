package leshan.client.lwm2m.resource;

import java.util.concurrent.ScheduledExecutorService;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.server.lwm2m.observation.ObserveSpec;

public interface LwM2mClientNode {

	public void read(LwM2mExchange exchange);

	public void observe(LwM2mExchange exchange, ScheduledExecutorService service);

	public void writeAttributes(LwM2mExchange exchange, ObserveSpec spec);

}
