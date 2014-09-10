package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.ExecuteResponse;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.ReadResponse;
import leshan.client.lwm2m.operation.WriteResponse;

public class StringLwM2mResource implements LwM2mResource {

	@Override
	public final void read(final LwM2mExchange exchange) {
		handleRead(new StringLwM2mExchange(exchange));
	}

	protected void handleRead(final StringLwM2mExchange exchange) {
		exchange.advanced().respond(ReadResponse.notAllowed());
	}

	@Override
	public final void write(final LwM2mExchange exchange) {
		handleWrite(new StringLwM2mExchange(exchange));
	}

	protected void handleWrite(final StringLwM2mExchange exchange) {
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
