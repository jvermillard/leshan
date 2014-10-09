package leshan.client.lwm2m.resource;

import java.util.concurrent.ScheduledExecutorService;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.exchange.ObserveNotifyExchange;
import leshan.client.lwm2m.response.WriteResponse;
import leshan.server.lwm2m.observation.ObserveSpec;

public abstract class LwM2mClientNode {

	protected ObserveSpec observeSpec;
	protected ObserveNotifyExchange observer;

	public LwM2mClientNode() {
		this.observeSpec = new ObserveSpec.Builder().build();
	}

	public abstract void read(LwM2mExchange exchange);

	public void observe(final LwM2mExchange exchange, final ScheduledExecutorService service) {
		observer = new ObserveNotifyExchange(exchange, this, observeSpec, service);
	}

	public void write(LwM2mExchange exchange) {
		exchange.respond(WriteResponse.notAllowed());
	}

	public void writeAttributes(LwM2mExchange exchange, ObserveSpec spec) {
		this.observeSpec = spec;
		exchange.respond(WriteResponse.success());
	}

}
