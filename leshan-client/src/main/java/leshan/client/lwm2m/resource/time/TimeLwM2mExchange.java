package leshan.client.lwm2m.resource.time;

import java.util.Date;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.TypedLwM2mExchange;

public class TimeLwM2mExchange extends TypedLwM2mExchange<Date> {

	public TimeLwM2mExchange(final LwM2mExchange exchange) {
		super(exchange);
	}

	@Override
	protected Date convertFromBytes(final byte[] value) {
		final int secondsSinceEpoch = Integer.parseInt(new String(value));
		final long millisSinceEpoch = secondsSinceEpoch * 1000;
		return new Date(millisSinceEpoch);
	}

	@Override
	protected byte[] convertToBytes(final Date value) {
		final long secondsSinceEpoch = value.getTime() / 1000;
		return Long.toString(secondsSinceEpoch).getBytes();
	}

}
