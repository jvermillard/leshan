package leshan.client.lwm2m.exchange.aggregate;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.response.LwM2mResponse;
import leshan.server.lwm2m.impl.tlv.Tlv;

public class LwM2mObjectInstanceReadResponseAggregator extends LwM2mReadResponseAggregator {

	public LwM2mObjectInstanceReadResponseAggregator(final LwM2mExchange exchange, final int numExpectedResults) {
		super(exchange, numExpectedResults);
	}

	@Override
	protected Tlv createTlv(final int id, final LwM2mResponse response) {
		Tlv result = response.getResponsePayloadAsTlv();
		result.setIdentifier(id);
		return result;
	}

}
