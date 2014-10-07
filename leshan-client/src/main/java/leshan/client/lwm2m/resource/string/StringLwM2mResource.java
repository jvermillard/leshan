package leshan.client.lwm2m.resource.string;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.BaseTypedLwM2mResource;

public class StringLwM2mResource extends BaseTypedLwM2mResource<StringLwM2mExchange> {

	@Override
	protected StringLwM2mExchange createSpecificExchange(final LwM2mExchange exchange) {
		return new StringLwM2mExchange(exchange);
	}

}
