package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mExchange;

public class StringLwM2mExchange extends TypedLwM2mExchange<String> {

	public StringLwM2mExchange(final LwM2mExchange exchange) {
		super(exchange);
	}

	@Override
	protected String convertFromBytes(final byte[] value) {
		return new String(value);
	}

	@Override
	protected byte[] convertToBytes(final String value) {
		return value.getBytes();
	}

}
