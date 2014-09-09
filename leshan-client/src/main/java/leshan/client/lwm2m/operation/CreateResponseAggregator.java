package leshan.client.lwm2m.operation;

import java.util.Collection;
import java.util.Map;

public class CreateResponseAggregator extends LwM2mResponseAggregator {

	private final int instanceId;

	public CreateResponseAggregator(final LwM2mCreateExchange exchange,
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
