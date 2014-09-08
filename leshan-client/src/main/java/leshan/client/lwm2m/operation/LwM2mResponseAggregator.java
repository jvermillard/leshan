package leshan.client.lwm2m.operation;

import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvEncoder;

public abstract class LwM2mResponseAggregator {

	private final LwM2mExchange exchange;
	private final Map<Integer, LwM2mResponse> responses;
	private final int numExpectedResults;

	public LwM2mResponseAggregator(final LwM2mExchange exchange, final int numExpectedResults) {
		this.exchange = exchange;
		this.responses = new ConcurrentHashMap<>();
		this.numExpectedResults = numExpectedResults;
	}

	public void respond(final int id, final LwM2mResponse response) {
		responses.put(id, response);
		if (responses.size() == numExpectedResults) {
			sendResponseToCoapExchange();
		}
	}

	private void sendResponseToCoapExchange() {
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
