package leshan.client.lwm2m.response;

import static leshan.client.lwm2m.response.OperationResponseCode.CONTENT;
import static leshan.client.lwm2m.response.OperationResponseCode.METHOD_NOT_ALLOWED;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import leshan.server.lwm2m.impl.tlv.Tlv;
import leshan.server.lwm2m.impl.tlv.Tlv.TlvType;
import leshan.server.lwm2m.impl.tlv.TlvDecoder;
import leshan.server.lwm2m.impl.tlv.TlvEncoder;

public class ReadResponse extends BaseLwM2mResponse {

	private ReadResponse(final OperationResponseCode code, final byte[] payload) {
		super(code, payload);
	}

	private ReadResponse(final OperationResponseCode code) {
		this(code, new byte[0]);
	}

	public static ReadResponse success(final byte[] readValue) {
		return new ReadResponse(CONTENT, readValue);
	}

	public static ReadResponse successMultiple(final Map<Integer, byte[]> readValues) {
		return new MultipleReadResponse(CONTENT, readValues);
	}

	// TODO Evaluate whether this needs to be used
	public static ReadResponse failure() {
		return new ReadResponse(METHOD_NOT_ALLOWED);
	}

	public static ReadResponse notAllowed() {
		return new ReadResponse(METHOD_NOT_ALLOWED);
	}

	private static class MultipleReadResponse extends ReadResponse {

		private Tlv tlvPayload;

		public MultipleReadResponse(OperationResponseCode code, Map<Integer, byte[]> readValues) {
			super(code, getPayload(readValues));
			tlvPayload = new Tlv(TlvType.MULTIPLE_RESOURCE, TlvDecoder.decode(ByteBuffer.wrap(getResponsePayload())),
					null, 0);
		}

		@Override
		public Tlv getResponsePayloadAsTlv() {
			return tlvPayload;
		}

	}

	private static byte[] getPayload(Map<Integer, byte[]> readValues) {
		List<Tlv> children = new ArrayList<Tlv>();
		for(Entry<Integer, byte[]> entry : new TreeMap<>(readValues).entrySet()) {
			children.add(new Tlv(TlvType.RESOURCE_INSTANCE, null, entry.getValue(), entry.getKey()));
		}
		return TlvEncoder.encode(children.toArray(new Tlv[0])).array();
	}

}
