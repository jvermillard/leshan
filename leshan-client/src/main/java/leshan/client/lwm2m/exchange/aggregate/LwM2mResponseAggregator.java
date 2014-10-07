package leshan.client.lwm2m.exchange.aggregate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.response.LwM2mResponse;

public abstract class LwM2mResponseAggregator {

	private final LwM2mExchange exchange;
	private final Map<Integer, LwM2mResponse> responses;
	private final int numExpectedResults;

	public LwM2mResponseAggregator(final LwM2mExchange exchange, final int numExpectedResults) {
		this.exchange = exchange;
		this.responses = new ConcurrentHashMap<>();
		this.numExpectedResults = numExpectedResults;
		respondIfReady();
	}

	public void respond(final int id, final LwM2mResponse response) {
		responses.put(id, response);
		respondIfReady();
	}

	private void respondIfReady() {
		if (responses.size() == numExpectedResults) {
			respondToExchange(responses, exchange);
		}
	}

	protected abstract void respondToExchange(Map<Integer, LwM2mResponse> responses, LwM2mExchange exchange);

	public LwM2mExchange getUnderlyingExchange() {
		return exchange;
	}

}
