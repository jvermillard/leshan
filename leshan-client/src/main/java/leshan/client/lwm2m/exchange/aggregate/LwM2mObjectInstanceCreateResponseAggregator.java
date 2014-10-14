package leshan.client.lwm2m.exchange.aggregate;

import java.util.Collection;
import java.util.Map;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.response.CreateResponse;
import leshan.client.lwm2m.response.LwM2mResponse;

public class LwM2mObjectInstanceCreateResponseAggregator extends LwM2mResponseAggregator {

	private final int instanceId;

	public LwM2mObjectInstanceCreateResponseAggregator(final LwM2mExchange exchange,
			final int numExpectedResults, final int instanceId) {
		super(exchange, numExpectedResults);
		this.instanceId = instanceId;
	}

	@Override
	protected void respondToExchange(final Map<Integer, LwM2mResponse> responses,
			final LwM2mExchange exchange) {
		if (isSuccess(responses.values())) {
			exchange.respond(CreateResponse.success(instanceId));
		} else {
			exchange.respond(CreateResponse.methodNotAllowed());
		}
	}

	private boolean isSuccess(final Collection<LwM2mResponse> values) {
		for (final LwM2mResponse response : values) {
			if (!response.isSuccess()) {
				return false;
			}
		}
		return true;
	}

}
