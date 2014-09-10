package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.ReadResponse;
import leshan.client.lwm2m.operation.WriteResponse;

public class StringLwM2mExchange {

	private final LwM2mExchange exchange;

	public StringLwM2mExchange(final LwM2mExchange exchange) {
		this.exchange = exchange;
	}

	public String getRequestPayload() {
		return new String(exchange.getRequestPayload());
	}

	public void respondContent(final String value) {
		exchange.respond(ReadResponse.success(value.getBytes()));
	}

	public void respondSuccess() {
		exchange.respond(WriteResponse.success());
	}

	public LwM2mExchange advanced() {
		return exchange;
	}

}
