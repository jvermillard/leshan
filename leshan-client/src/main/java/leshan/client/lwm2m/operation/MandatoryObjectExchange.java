package leshan.client.lwm2m.operation;

import leshan.client.lwm2m.californium.Callback;
import leshan.client.lwm2m.resource.LwM2mObjectInstance;
import leshan.server.lwm2m.observation.ObserveSpec;

public class MandatoryObjectExchange implements LwM2mCreateExchange {

	private final Callback<LwM2mObjectInstance> callback;
	private LwM2mObjectInstance instance;
	private final byte[] payload;

	public MandatoryObjectExchange(final Callback<LwM2mObjectInstance> callback, final byte[] payload) {
		this.callback = callback;
		this.payload = payload;
	}

	@Override
	public void respond(final LwM2mResponse response) {
		if(response.isSuccess()){
			callback.onSuccess(instance);
		}
		else{
			System.err.println(response.getCode());
			callback.onFailure();
		}
	}

	@Override
	public byte[] getRequestPayload() {
		return payload;
	}

	@Override
	public boolean hasObjectInstanceId() {
		return false;
	}

	@Override
	public int getObjectInstanceId() {
		return 0;
	}

	@Override
	public boolean isObserve() {
		return false;
	}

	@Override
	public ObserveSpec getObserveSpec() {
		return null;
	}

	@Override
	public void setObjectInstance(final LwM2mObjectInstance instance) {
		this.instance = instance;
	}

}
