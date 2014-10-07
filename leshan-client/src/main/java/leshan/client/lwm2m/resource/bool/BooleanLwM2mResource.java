package leshan.client.lwm2m.resource.bool;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.BaseTypedLwM2mResource;

public class BooleanLwM2mResource extends BaseTypedLwM2mResource<BooleanLwM2mExchange> {

	@Override
	protected BooleanLwM2mExchange createSpecificExchange(final LwM2mExchange exchange) {
		return new BooleanLwM2mExchange(exchange);
	}

}
