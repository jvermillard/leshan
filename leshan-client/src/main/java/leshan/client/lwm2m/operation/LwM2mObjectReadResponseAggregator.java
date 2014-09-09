package leshan.client.lwm2m.operation;

import java.nio.ByteBuffer;

import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvDecoder;
import leshan.server.lwm2m.tlv.TlvType;

public class LwM2mObjectReadResponseAggregator extends LwM2mReadResponseAggregator {

	public LwM2mObjectReadResponseAggregator(final LwM2mExchange exchange, final int numExpectedResults) {
		super(exchange, numExpectedResults);
	}

	@Override
	protected Tlv createTlv(final int id, final LwM2mResponse response) {
		return new Tlv(TlvType.OBJECT_INSTANCE, TlvDecoder.decode(ByteBuffer.wrap(response.getResponsePayload())),
				null, id);
	}

//	@Override
//	protected void respondToExchange(final Map<Integer, LwM2mResponse> responses, final LwM2mExchange exchange) {
//		final TreeMap<Integer, LwM2mResponse> sortedResponses = new TreeMap<>(responses);
//		final Queue<Tlv> tlvs = new LinkedList<Tlv>();
//		for (final Entry<Integer, LwM2mResponse> entry : sortedResponses.entrySet()) {
//			final int id2 = entry.getKey();
//			final LwM2mResponse response2 = entry.getValue();
//			tlvs.add(createTlv(id2, response2));
//		}
//		final byte[] payload = TlvEncoder.encode(tlvs.toArray(new Tlv[0])).array();
//		exchange.respond(ReadResponse.success(payload));
//	}
//
}
