package leshan.client.lwm2m.resource.opaque;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.TypedLwM2mExchange;

public class OpaqueLwM2mExchange extends TypedLwM2mExchange<byte[]> {

	public OpaqueLwM2mExchange(final LwM2mExchange exchange) {
		super(exchange);
	}

	@Override
	protected byte[] convertFromBytes(final byte[] value) {
		return value;
	}

	@Override
	protected byte[] convertToBytes(final byte[] value) {
		return value;
	}

}
