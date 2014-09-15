package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mExchange;

public class OpaqueLwM2mResource extends BaseTypedLwM2mResource<OpaqueLwM2mExchange> {

	@Override
	protected OpaqueLwM2mExchange createSpecificExchange(final LwM2mExchange exchange) {
		return new OpaqueLwM2mExchange(exchange);
	}

}
