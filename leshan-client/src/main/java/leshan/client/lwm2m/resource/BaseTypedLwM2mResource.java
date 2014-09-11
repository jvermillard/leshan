package leshan.client.lwm2m.resource;

import java.util.HashSet;
import java.util.Set;

import leshan.client.lwm2m.operation.ExecuteResponse;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.ReadResponse;
import leshan.client.lwm2m.operation.WriteResponse;

public abstract class BaseTypedLwM2mResource<E extends TypedLwM2mExchange<?>> implements LwM2mResource {

	protected abstract E createSpecificExchange(final LwM2mExchange exchange);

	private final Set<LwM2mExchange> observers = new HashSet<>();

	@Override
	public final void read(final LwM2mExchange exchange) {
		if(exchange.isObserve()) {
			observers.add(exchange);
		}
		handleRead(createSpecificExchange(exchange));
	}

	protected void handleRead(final E exchange) {
		exchange.advanced().respond(ReadResponse.notAllowed());
	}

	@Override
	public final void write(final LwM2mExchange exchange) {
		try {
			handleWrite(createSpecificExchange(exchange));
		} catch(final Exception e) {
			exchange.respond(WriteResponse.badRequest());
		}
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
	public void observe(final Notifier notifier) {

	}

	@Override
	public boolean isReadable() {
		return false;
	}

	protected final void notifyResourceUpdated() {
		for(final LwM2mExchange exchange : observers) {
			read(exchange);
		}
	}

}
