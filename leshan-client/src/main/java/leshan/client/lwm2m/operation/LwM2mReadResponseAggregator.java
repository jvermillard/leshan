package leshan.client.lwm2m.operation;

public abstract class LwM2mReadResponseAggregator extends LwM2mResponseAggregator {

	public LwM2mReadResponseAggregator(final LwM2mExchange exchange, final int numExpectedResults) {
		super(exchange, numExpectedResults);
	}

}
