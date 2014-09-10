package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mExchange;

public class StringLwM2mResource extends BaseTypedLwM2mResource<StringLwM2mExchange> {

	@Override
	protected StringLwM2mExchange createSpecificExchange(final LwM2mExchange exchange) {
		return new StringLwM2mExchange(exchange);
	}

}
