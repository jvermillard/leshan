package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.response.ExecuteResponse;
import leshan.client.lwm2m.response.ReadResponse;
import leshan.client.lwm2m.response.WriteResponse;

public abstract class BaseTypedLwM2mResource<E extends TypedLwM2mExchange<?>> extends LwM2mClientResource {

	protected abstract E createSpecificExchange(final LwM2mExchange exchange);

	@Override
	public final void read(final LwM2mExchange exchange) {
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

	protected void handleExecute(final LwM2mExchange exchange) {
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
