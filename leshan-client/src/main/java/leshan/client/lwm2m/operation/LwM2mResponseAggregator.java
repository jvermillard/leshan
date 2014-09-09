package leshan.client.lwm2m.operation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class LwM2mResponseAggregator {

	private final LwM2mExchange exchange;
	private final Map<Integer, LwM2mResponse> responses;
	private final int numExpectedResults;

	public LwM2mResponseAggregator(final LwM2mExchange exchange, final int numExpectedResults) {
		this.exchange = exchange;
		this.responses = new ConcurrentHashMap<>();
		this.numExpectedResults = numExpectedResults;
	}

	public void respond(final int id, final LwM2mResponse response) {
		responses.put(id, response);
		if (responses.size() == numExpectedResults) {
			respondToExchange(responses, exchange);
		}
	}

	protected abstract void respondToExchange(Map<Integer, LwM2mResponse> responses, LwM2mExchange exchange);

}
