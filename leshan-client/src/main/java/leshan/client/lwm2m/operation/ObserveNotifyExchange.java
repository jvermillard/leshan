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
		if (shouldNotifyRange(response)) {
			exchange.respond(ObserveResponse.notifyWithContent(response.getResponsePayload()));
		}
	}

	private boolean shouldNotifyRange(final LwM2mResponse response) {
		try {
			final float value = Float.parseFloat(new String(response.getResponsePayload()));
			final Float greaterThan = observeSpec.getGreaterThan();
			final Float lessThan = observeSpec.getLessThan();
			return (greaterThan != null && value > greaterThan) ||
					(lessThan != null && value < lessThan) ||
					(greaterThan == null && lessThan == null);
		} catch (final NumberFormatException e) {
			return true;
		}
	}

}
