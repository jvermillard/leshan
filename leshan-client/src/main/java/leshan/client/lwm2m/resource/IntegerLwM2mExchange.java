package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.ReadResponse;
import leshan.client.lwm2m.operation.WriteResponse;

public class IntegerLwM2mExchange {

	private final LwM2mExchange exchange;

	public IntegerLwM2mExchange(final LwM2mExchange exchange) {
		this.exchange = exchange;
	}

	public int getRequestPayload() {
		return Integer.parseInt(new String(exchange.getRequestPayload()));
	}

	public void respondContent(final int value) {
		exchange.respond(ReadResponse.success(Integer.toString(value).getBytes()));
	}

	public void respondSuccess() {
		exchange.respond(WriteResponse.success());
	}

	public LwM2mExchange advanced() {
		return exchange;
	}

}
