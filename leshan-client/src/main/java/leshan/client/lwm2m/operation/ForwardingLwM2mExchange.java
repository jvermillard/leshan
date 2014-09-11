package leshan.client.lwm2m.operation;

public class ForwardingLwM2mExchange implements LwM2mExchange {

	protected final LwM2mExchange exchange;

	public ForwardingLwM2mExchange(final LwM2mExchange exchange) {
		this.exchange = exchange;
	}

	@Override
	public void respond(final LwM2mResponse response) {
		exchange.respond(response);
	}

	@Override
	public byte[] getRequestPayload() {
		return exchange.getRequestPayload();
	}

	@Override
	public boolean hasObjectInstanceId() {
		return exchange.hasObjectInstanceId();
	}

	@Override
	public int getObjectInstanceId() {
		return exchange.getObjectInstanceId();
	}

	@Override
	public boolean isObserve() {
		return exchange.isObserve();
	}

}
