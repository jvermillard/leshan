package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mExchange;

public class IntegerLwM2mResource extends BaseTypedLwM2mResource<IntegerLwM2mExchange> {

	@Override
	protected IntegerLwM2mExchange createSpecificExchange(final LwM2mExchange exchange) {
		return new IntegerLwM2mExchange(exchange);
	}

}
