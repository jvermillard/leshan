package leshan.client.lwm2m.operation;

import ch.ethz.inf.vs.californium.server.resources.CoapExchange;

public class ClientObservationImpl implements ClientObservation {

	private final CoapExchange exchange;

	public ClientObservationImpl(final CoapExchange exchange) {
		this.exchange = exchange;
	}

}
