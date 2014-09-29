package leshan.client.lwm2m.operation;

import leshan.client.lwm2m.response.OperationResponseCode;
import leshan.server.lwm2m.tlv.Tlv;

public interface LwM2mResponse {

	public OperationResponseCode getCode();

	public byte[] getResponsePayload();

	public boolean isSuccess();

	Tlv getResponsePayloadAsTlv();

}
