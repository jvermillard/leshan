package leshan.client.lwm2m.operation;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

public interface LwM2mResponse {

	public ResponseCode getCode();

	public byte[] getResponsePayload();

}
