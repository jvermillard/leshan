package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mExchange;

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
