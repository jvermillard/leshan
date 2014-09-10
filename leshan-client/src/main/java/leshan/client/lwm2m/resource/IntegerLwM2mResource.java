package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.ExecuteResponse;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.ReadResponse;
import leshan.client.lwm2m.operation.WriteResponse;

public class IntegerLwM2mResource implements LwM2mResource {

	@Override
	public final void read(final LwM2mExchange exchange) {
		handleRead(new IntegerLwM2mExchange(exchange));
	}

	protected void handleRead(final IntegerLwM2mExchange exchange) {
		exchange.advanced().respond(ReadResponse.notAllowed());
	}

	@Override
	public final void write(final LwM2mExchange exchange) {
		// TODO: Add encoding if needed
		try {
			handleWrite(new IntegerLwM2mExchange(exchange));
		} catch(final NumberFormatException e) {
			exchange.respond(WriteResponse.badRequest());
		}

	}

	protected void handleWrite(final IntegerLwM2mExchange exchange) {
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

}
