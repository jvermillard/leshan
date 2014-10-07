package leshan.client.lwm2m.resource.time;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.BaseTypedLwM2mResource;
import leshan.client.lwm2m.resource.opaque.OpaqueLwM2mExchange;

public class TimeLwM2mResource extends BaseTypedLwM2mResource<OpaqueLwM2mExchange> {

	@Override
	protected OpaqueLwM2mExchange createSpecificExchange(final LwM2mExchange exchange) {
		return new OpaqueLwM2mExchange(exchange);
	}

}
