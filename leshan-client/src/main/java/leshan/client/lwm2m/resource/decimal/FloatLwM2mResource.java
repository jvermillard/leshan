package leshan.client.lwm2m.resource.decimal;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.BaseTypedLwM2mResource;

public class FloatLwM2mResource extends BaseTypedLwM2mResource<FloatLwM2mExchange> {

	@Override
	protected FloatLwM2mExchange createSpecificExchange(final LwM2mExchange exchange) {
		return new FloatLwM2mExchange(exchange);
	}

}
