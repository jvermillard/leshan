package leshan.client.lwm2m.resource.time;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.BaseTypedLwM2mResource;

public class TimeLwM2mResource extends BaseTypedLwM2mResource<TimeLwM2mExchange> {

	@Override
	protected TimeLwM2mExchange createSpecificExchange(final LwM2mExchange exchange) {
		return new TimeLwM2mExchange(exchange);
	}

}
