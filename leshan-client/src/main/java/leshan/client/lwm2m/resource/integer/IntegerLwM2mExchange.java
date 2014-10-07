package leshan.client.lwm2m.resource.integer;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.TypedLwM2mExchange;

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
