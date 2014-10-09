package leshan.client.lwm2m.resource;

import java.util.concurrent.ScheduledExecutorService;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.response.WriteResponse;
import leshan.server.lwm2m.observation.ObserveSpec;

public abstract class LwM2mClientNode {

	public abstract void read(LwM2mExchange exchange);
	public abstract void observe(LwM2mExchange exchange, ScheduledExecutorService service);

	public void write(LwM2mExchange exchange) {
		exchange.respond(WriteResponse.notAllowed());
	}

	public abstract void writeAttributes(LwM2mExchange exchange, ObserveSpec spec);

}
