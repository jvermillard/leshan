package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mExchange;

public class MultipleLwM2mResource extends BaseTypedLwM2mResource<MultipleLwM2mExchange> {

	@Override
	protected MultipleLwM2mExchange createSpecificExchange(final LwM2mExchange exchange) {
		return new MultipleLwM2mExchange(exchange);
	}

}
