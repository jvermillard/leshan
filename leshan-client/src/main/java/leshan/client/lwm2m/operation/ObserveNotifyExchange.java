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

	/**
	 * This method checks the value provided in the response against the range
	 * in the ObserveSpec object. It will return true if ANY of the following
	 * are true:
	 *  - The value is not numeric
	 *  - The ObserveSpec specifies neither a "gt" nor an "lt" parameter
	 *  - The ObserveSpec specifies a "gt" parameter and the value is greater
	 *    than the parameter specified
	 *  - The ObserveSpec specifies an "lt" parameter and the value is less
	 *    than the parameter specified
	 *
	 * @param response Response containing the value that may or may not be
	 * notified
	 * @return A boolean indicating whether the response should be notified
	 * with respect to the ObserveSpec
	 */
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
