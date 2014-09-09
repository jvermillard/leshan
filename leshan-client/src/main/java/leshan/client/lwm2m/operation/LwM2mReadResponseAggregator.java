package leshan.client.lwm2m.operation;

import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TreeMap;

import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvEncoder;

public abstract class LwM2mReadResponseAggregator extends LwM2mResponseAggregator {

	public LwM2mReadResponseAggregator(final LwM2mExchange exchange, final int numExpectedResults) {
		super(exchange, numExpectedResults);
	}

	@Override
	protected void respondToExchange(final Map<Integer, LwM2mResponse> responses, final LwM2mExchange exchange) {
		final TreeMap<Integer, LwM2mResponse> sortedResponses = new TreeMap<>(responses);
		final Queue<Tlv> tlvs = new LinkedList<Tlv>();
		for (final Entry<Integer, LwM2mResponse> entry : sortedResponses.entrySet()) {
			final int id2 = entry.getKey();
			final LwM2mResponse response2 = entry.getValue();
			tlvs.add(createTlv(id2, response2));
		}
		final byte[] payload = TlvEncoder.encode(tlvs.toArray(new Tlv[0])).array();
		exchange.respond(ReadResponse.success(payload));
	}

	protected abstract Tlv createTlv(final int id, final LwM2mResponse response);

}
