package leshan.client.lwm2m.operation;

import java.util.Arrays;
import java.util.Objects;

import leshan.client.lwm2m.response.OperationResponseCode;
import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvType;

public abstract class BaseLwM2mResponse implements LwM2mResponse {

	private final OperationResponseCode code;
	private final byte[] payload;

	public BaseLwM2mResponse(final OperationResponseCode code, final byte[] payload) {
		this.code = code;
		this.payload = payload;
	}

	@Override
	public OperationResponseCode getCode() {
		return code;
	}

	@Override
	public byte[] getResponsePayload() {
		return payload;
	}

	@Override
	public Tlv getResponsePayloadAsTlv() {
		return new Tlv(TlvType.RESOURCE_VALUE, null, payload, 0);
	}

	@Override
	public boolean isSuccess() {
		return OperationResponseCode.isSuccess(code);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof BaseLwM2mResponse)) {
			return false;
		}
		final BaseLwM2mResponse other = (BaseLwM2mResponse)o;
		return code == other.code && Arrays.equals(payload, other.payload);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, Arrays.hashCode(payload));
	}

	@Override
	public String toString() {
		String payloadString = (payload == null) ? "" : ", \"" + new String(payload) + "\"";
		return "[" + getClass().getSimpleName() + ": " + code + payloadString + "]";
	}

}
