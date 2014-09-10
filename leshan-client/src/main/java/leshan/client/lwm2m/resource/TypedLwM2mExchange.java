package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.ReadResponse;
import leshan.client.lwm2m.operation.WriteResponse;

public abstract class TypedLwM2mExchange<T> {

	private final LwM2mExchange exchange;

	public TypedLwM2mExchange(final LwM2mExchange exchange) {
		this.exchange = exchange;
	}

	public final LwM2mExchange advanced() {
		return exchange;
	}

	public final void respondSuccess() {
		exchange.respond(WriteResponse.success());
	}

	public final void respondFailure() {
		exchange.respond(WriteResponse.failure());
	}

	public final T getRequestPayload() {
		final byte[] requestPayload = exchange.getRequestPayload();
		return convertFromBytes(requestPayload);
	}

	public final void respondContent(final T value) {
		exchange.respond(ReadResponse.success(convertToBytes(value)));
	}

	protected abstract T convertFromBytes(final byte[] value);

	protected abstract byte[] convertToBytes(final T value);

}
