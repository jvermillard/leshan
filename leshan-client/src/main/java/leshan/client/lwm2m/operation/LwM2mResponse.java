package leshan.client.lwm2m.operation;

import leshan.client.lwm2m.response.OperationResponseCode;

public interface LwM2mResponse {

	public OperationResponseCode getCode();

	public byte[] getResponsePayload();

	public boolean isSuccess();

}
