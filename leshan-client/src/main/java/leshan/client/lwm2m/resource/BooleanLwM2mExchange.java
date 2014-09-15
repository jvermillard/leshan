package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mExchange;

public class BooleanLwM2mExchange extends TypedLwM2mExchange<Boolean> {

	private static String ZERO = Integer.toString(0);
	private static String ONE = Integer.toString(1);

	public BooleanLwM2mExchange(final LwM2mExchange exchange) {
		super(exchange);
	}

	@Override
	protected Boolean convertFromBytes(final byte[] value) {
		final String parsedValue = new String(value);
		if(!parsedValue.equals(ZERO) && !parsedValue.equals(ONE)) {
			throw new IllegalArgumentException();
		}

		return Boolean.parseBoolean(parsedValue);
	}

	@Override
	protected byte[] convertToBytes(final Boolean value) {
		final int numericalValue = value ? 1 : 0;
		return Integer.toString(numericalValue).getBytes();
	}

}
