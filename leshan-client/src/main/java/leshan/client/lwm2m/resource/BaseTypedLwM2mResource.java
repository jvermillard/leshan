package leshan.client.lwm2m.resource;

import java.util.concurrent.ScheduledExecutorService;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.exchange.ObserveNotifyExchange;
import leshan.client.lwm2m.response.ExecuteResponse;
import leshan.client.lwm2m.response.ReadResponse;
import leshan.client.lwm2m.response.WriteResponse;
import leshan.server.lwm2m.observation.ObserveSpec;

public abstract class BaseTypedLwM2mResource<E extends TypedLwM2mExchange<?>> implements LwM2mClientResource {

	protected abstract E createSpecificExchange(final LwM2mExchange exchange);

	private ObserveNotifyExchange observer;
	private ObserveSpec observeSpec;

	public BaseTypedLwM2mResource() {
		this.observeSpec = new ObserveSpec.Builder().build();
	}

	@Override
	public final void read(final LwM2mExchange exchange) {
		handleRead(createSpecificExchange(exchange));
	}

	@Override
	public final void observe(final LwM2mExchange exchange, final ScheduledExecutorService service) {
		observer = new ObserveNotifyExchange(exchange, this, observeSpec, service);
	}

	protected void handleRead(final E exchange) {
		exchange.advanced().respond(ReadResponse.notAllowed());
	}

	@Override
	public final void write(final LwM2mExchange exchange) {
		try {
			final ObserveSpec spec = exchange.getObserveSpec();
			if (spec != null) {
				writeAttributes(exchange, spec);
			} else {
				handleWrite(createSpecificExchange(exchange));
			}
		} catch(final Exception e) {
			exchange.respond(WriteResponse.badRequest());
		}
	}

	@Override
	public void writeAttributes(final LwM2mExchange exchange, final ObserveSpec spec) {
		observeSpec = spec;
		exchange.respond(WriteResponse.success());
	}

	protected void handleWrite(final E exchange) {
		exchange.advanced().respond(WriteResponse.notAllowed());
	}

	@Override
	public void execute(final LwM2mExchange exchange) {
		handleExecute(exchange);
	}

	public void handleExecute(final LwM2mExchange exchange) {
		exchange.respond(ExecuteResponse.notAllowed());
	}

	@Override
	public boolean isReadable() {
		return false;
	}

	@Override
	public final void notifyResourceUpdated() {
		if (observer != null) {
			observer.setObserveSpec(observeSpec);
			read(observer);
		}
	}

}
