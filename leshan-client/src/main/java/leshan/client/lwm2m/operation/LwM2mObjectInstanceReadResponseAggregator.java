package leshan.client.lwm2m.operation;

import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvType;

public class LwM2mObjectInstanceReadResponseAggregator extends LwM2mReadResponseAggregator {

	public LwM2mObjectInstanceReadResponseAggregator(final LwM2mExchange exchange, final int numExpectedResults) {
		super(exchange, numExpectedResults);
	}

	@Override
	protected Tlv createTlv(final int id, final LwM2mResponse response) {
		return new Tlv(TlvType.RESOURCE_VALUE, null, response.getResponsePayload(), id);
	}

}
