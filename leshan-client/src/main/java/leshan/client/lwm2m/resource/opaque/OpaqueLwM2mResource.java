package leshan.client.lwm2m.resource.opaque;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.BaseTypedLwM2mResource;

public class OpaqueLwM2mResource extends BaseTypedLwM2mResource<OpaqueLwM2mExchange> {

	@Override
	protected OpaqueLwM2mExchange createSpecificExchange(final LwM2mExchange exchange) {
		return new OpaqueLwM2mExchange(exchange);
	}

}
