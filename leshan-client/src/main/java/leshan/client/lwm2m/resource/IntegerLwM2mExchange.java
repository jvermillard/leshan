package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mExchange;

public class IntegerLwM2mExchange extends TypedLwM2mExchange<Integer> {

	public IntegerLwM2mExchange(final LwM2mExchange exchange) {
		super(exchange);
	}

	@Override
	protected Integer convertFromBytes(final byte[] value) {
		return Integer.parseInt(new String(value));
	}

	@Override
	protected byte[] convertToBytes(final Integer value) {
		return Integer.toString(value).getBytes();
	}

}
