package leshan.client.lwm2m.resource.multiple;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.BaseTypedLwM2mResource;

public class MultipleLwM2mResource extends BaseTypedLwM2mResource<MultipleLwM2mExchange> {

	@Override
	protected MultipleLwM2mExchange createSpecificExchange(final LwM2mExchange exchange) {
		return new MultipleLwM2mExchange(exchange);
	}

}
