package leshan.client.lwm2m.report;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ch.ethz.inf.vs.californium.network.Exchange;

public enum Observations {
	INSTANCE;

	private final Map<byte[], Exchange> observationMap = new ConcurrentHashMap<>();

	public void addObserverForToken(final byte[] token, final Exchange exchange) {
		observationMap.put(token, exchange);
	}

	public Exchange getExchangesForToken(final byte[] token) {
		return observationMap.get(token);
	}
}
