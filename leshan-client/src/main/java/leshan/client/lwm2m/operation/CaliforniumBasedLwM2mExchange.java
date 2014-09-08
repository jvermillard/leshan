package leshan.client.lwm2m.operation;

import ch.ethz.inf.vs.californium.server.resources.CoapExchange;

public class CaliforniumBasedLwM2mExchange implements LwM2mExchange {

	private final CoapExchange exchange;

	public CaliforniumBasedLwM2mExchange(final CoapExchange exchange) {
		this.exchange = exchange;
	}

	@Override
	public void respond(final LwM2mResponse response) {
		exchange.respond(response.getCode(), response.getResponsePayload());
	}

	@Override
	public byte[] getRequestPayload() {
		return exchange.getRequestPayload();
	}

}
