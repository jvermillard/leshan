package leshan.client.lwm2m.operation;

import leshan.server.lwm2m.observation.ObserveSpec;

public class ObserveNotifyExchange extends ForwardingLwM2mExchange {

	private final ObserveSpec observeSpec;

	public ObserveNotifyExchange(final LwM2mExchange exchange, final ObserveSpec observeSpec) {
		super(exchange);
		this.observeSpec = observeSpec;
	}

	@Override
	public void respond(final LwM2mResponse response) {
		final Float greaterThan = observeSpec.getGreaterThan();
		if (greaterThan == null || Float.parseFloat(new String(response.getResponsePayload())) > greaterThan) {
			exchange.respond(ObserveResponse.notifyWithContent(response.getResponsePayload()));
		}
	}

}
