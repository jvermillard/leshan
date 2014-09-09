package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.ExecuteResponse;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.ReadResponse;
import leshan.client.lwm2m.operation.WriteResponse;

public class SingleLwM2mResource implements LwM2mResource {

	@Override
	public final void read(final LwM2mExchange exchange) {
		handleRead(exchange);
	}

	protected void handleRead(final LwM2mExchange exchange) {
		exchange.respond(ReadResponse.notAllowed());
	}

	@Override
	public final void write(final LwM2mExchange exchange) {
		handleWrite(exchange);
	}

	protected void handleWrite(final LwM2mExchange exchange) {
		exchange.respond(WriteResponse.notAllowed());
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
