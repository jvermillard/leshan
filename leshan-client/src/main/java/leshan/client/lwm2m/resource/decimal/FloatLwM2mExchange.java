package leshan.client.lwm2m.resource.decimal;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.TypedLwM2mExchange;

public class FloatLwM2mExchange extends TypedLwM2mExchange<Float> {

	public FloatLwM2mExchange(final LwM2mExchange exchange) {
		super(exchange);
	}

	@Override
	protected Float convertFromBytes(final byte[] value) {
		return Float.parseFloat(new String(value));
	}

	@Override
	protected byte[] convertToBytes(final Float value) {
		return Float.toString(value).getBytes();
	}

}
