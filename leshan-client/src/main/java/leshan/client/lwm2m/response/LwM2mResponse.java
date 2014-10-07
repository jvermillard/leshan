package leshan.client.lwm2m.response;

import leshan.server.lwm2m.impl.tlv.Tlv;

public interface LwM2mResponse {

	public OperationResponseCode getCode();

	public byte[] getResponsePayload();

	public boolean isSuccess();

	Tlv getResponsePayloadAsTlv();

}
