package leshan.client.lwm2m.operation;

public class ObserveNotifyExchange extends ForwardingLwM2mExchange {

	public ObserveNotifyExchange(final LwM2mExchange exchange) {
		super(exchange);
	}

	@Override
	public void respond(final LwM2mResponse response) {
		exchange.respond(ObserveResponse.notifyWithContent(response.getResponsePayload()));
	}

}
