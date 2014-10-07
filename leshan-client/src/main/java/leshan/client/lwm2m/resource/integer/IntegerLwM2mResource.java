package leshan.client.lwm2m.resource.integer;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.BaseTypedLwM2mResource;

public class IntegerLwM2mResource extends BaseTypedLwM2mResource<IntegerLwM2mExchange> {

	@Override
	protected IntegerLwM2mExchange createSpecificExchange(final LwM2mExchange exchange) {
		return new IntegerLwM2mExchange(exchange);
	}

}
