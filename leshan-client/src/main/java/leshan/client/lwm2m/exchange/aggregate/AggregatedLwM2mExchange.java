package leshan.client.lwm2m.exchange.aggregate;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.response.LwM2mResponse;
import leshan.server.lwm2m.observation.ObserveSpec;


public class AggregatedLwM2mExchange implements LwM2mExchange {

	private final LwM2mResponseAggregator aggr;
	private final int id;
	private byte[] payload;

	public AggregatedLwM2mExchange(final LwM2mResponseAggregator aggr, final int id) {
		this.aggr = aggr;
		this.id = id;
	}

	@Override
	public void respond(final LwM2mResponse response) {
		aggr.respond(id, response);
	}

	@Override
	public byte[] getRequestPayload() {
		return payload;
	}

	public void setRequestPayload(final byte[] newPayload) {
		payload = newPayload;
	}

	@Override
	public boolean hasObjectInstanceId() {
		return false;
	}

	@Override
	public int getObjectInstanceId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isObserve() {
		return aggr.getUnderlyingExchange().isObserve();
	}

	@Override
	public ObserveSpec getObserveSpec() {
		return aggr.getUnderlyingExchange().getObserveSpec();
	}

}
