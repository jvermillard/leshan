package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mExchange;

public class BooleanLwM2mResource extends BaseTypedLwM2mResource<BooleanLwM2mExchange> {

	@Override
	protected BooleanLwM2mExchange createSpecificExchange(final LwM2mExchange exchange) {
		return new BooleanLwM2mExchange(exchange);
	}

}
