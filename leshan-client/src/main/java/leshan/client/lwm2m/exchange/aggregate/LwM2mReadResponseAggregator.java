package leshan.client.lwm2m.exchange.aggregate;

import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TreeMap;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.response.LwM2mResponse;
import leshan.client.lwm2m.response.ReadResponse;
import leshan.server.lwm2m.impl.tlv.Tlv;
import leshan.server.lwm2m.impl.tlv.TlvEncoder;

public abstract class LwM2mReadResponseAggregator extends LwM2mResponseAggregator {

	public LwM2mReadResponseAggregator(final LwM2mExchange exchange, final int numExpectedResults) {
		super(exchange, numExpectedResults);
	}

	@Override
	protected void respondToExchange(final Map<Integer, LwM2mResponse> responses, final LwM2mExchange exchange) {
		final TreeMap<Integer, LwM2mResponse> sortedResponses = new TreeMap<>(responses);
		final Queue<Tlv> tlvs = new LinkedList<Tlv>();
		for (final Entry<Integer, LwM2mResponse> entry : sortedResponses.entrySet()) {
			final int id = entry.getKey();
			final LwM2mResponse response = entry.getValue();
			if (response.isSuccess()) {
				tlvs.add(createTlv(id, response));
			}
		}
		final byte[] payload = TlvEncoder.encode(tlvs.toArray(new Tlv[0])).array();
		exchange.respond(ReadResponse.success(payload));
	}

	protected abstract Tlv createTlv(final int id, final LwM2mResponse response);

}
