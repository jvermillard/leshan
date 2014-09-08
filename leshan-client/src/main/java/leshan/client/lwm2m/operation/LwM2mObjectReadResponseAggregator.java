package leshan.client.lwm2m.operation;

import java.nio.ByteBuffer;

import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvDecoder;
import leshan.server.lwm2m.tlv.TlvType;

public class LwM2mObjectReadResponseAggregator extends LwM2mResponseAggregator {

	public LwM2mObjectReadResponseAggregator(final LwM2mExchange exchange, final int numExpectedResults) {
		super(exchange, numExpectedResults);
	}

	@Override
	protected Tlv createTlv(final int id, final LwM2mResponse response) {
		return new Tlv(TlvType.OBJECT_INSTANCE, TlvDecoder.decode(ByteBuffer.wrap(response.getResponsePayload())),
				null, id);
	}

}
