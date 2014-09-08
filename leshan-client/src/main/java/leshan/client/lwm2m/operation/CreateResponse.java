package leshan.client.lwm2m.operation;

import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CREATED;
import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.METHOD_NOT_ALLOWED;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

public abstract class CreateResponse implements LwM2mResponse {

	public static CreateResponse success(final int instanceId) {
		return new CreateResponse() {

			@Override
			public ResponseCode getCode() {
				return CREATED;
			}

			@Override
			public byte[] getResponsePayload() {
				return new byte[0];
			}

			@Override
			public String getLocation() {
				return Integer.toString(instanceId);
			}

		};
	}

	public static LwM2mResponse methodNotAllowed() {
		return new CreateResponse() {

			@Override
			public ResponseCode getCode() {
				return METHOD_NOT_ALLOWED;
			}

			@Override
			public byte[] getResponsePayload() {
				return new byte[0];
			}

			@Override
			public String getLocation() {
				return null;
			}
		};
	}

	public abstract String getLocation();

}
