package leshan.client.lwm2m.resource;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.server.lwm2m.impl.tlv.Tlv;
import leshan.server.lwm2m.impl.tlv.Tlv.TlvType;
import leshan.server.lwm2m.impl.tlv.TlvDecoder;
import leshan.server.lwm2m.impl.tlv.TlvEncoder;

public class MultipleLwM2mExchange extends TypedLwM2mExchange<Map<Integer, byte[]>> {

	public MultipleLwM2mExchange(final LwM2mExchange exchange) {
		super(exchange);
	}

	@Override
	protected Map<Integer, byte[]> convertFromBytes(final byte[] value) {
		final Tlv[] tlvs = TlvDecoder.decode(ByteBuffer.wrap(value));
		final Map<Integer, byte[]> result = new HashMap<>();
		for (final Tlv tlv : tlvs) {
			if (tlv.getType() != TlvType.RESOURCE_INSTANCE) {
				throw new IllegalArgumentException();
			}
			result.put(tlv.getIdentifier(), tlv.getValue());
		}
		return result;
	}

	@Override
	protected byte[] convertToBytes(final Map<Integer, byte[]> value) {
		final List<Tlv> tlvs = new ArrayList<>();
		for (final Entry<Integer, byte[]> entry : new TreeMap<Integer, byte[]>(value).entrySet()) {
			tlvs.add(new Tlv(TlvType.RESOURCE_INSTANCE, null, entry.getValue(), entry.getKey()));
		}
		return TlvEncoder.encode(tlvs.toArray(new Tlv[0])).array();
	}

}
