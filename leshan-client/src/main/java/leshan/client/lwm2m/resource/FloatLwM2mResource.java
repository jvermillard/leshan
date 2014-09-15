package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mExchange;

public class FloatLwM2mResource extends BaseTypedLwM2mResource<FloatLwM2mExchange> {

	@Override
	protected FloatLwM2mExchange createSpecificExchange(final LwM2mExchange exchange) {
		return new FloatLwM2mExchange(exchange);
	}

}
