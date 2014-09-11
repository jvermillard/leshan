package leshan.client.lwm2m.operation;


public class ObserveNotifyExchange implements LwM2mExchange {

	private final LwM2mExchange exchange;

	public ObserveNotifyExchange(final LwM2mExchange exchange) {
		this.exchange = exchange;
	}

	@Override
	public void respond(final LwM2mResponse response) {
		exchange.respond(ObserveResponse.notifyWithContent(response.getResponsePayload()));
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
