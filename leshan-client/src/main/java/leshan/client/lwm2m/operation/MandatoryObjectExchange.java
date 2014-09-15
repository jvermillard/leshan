package leshan.client.lwm2m.operation;

import leshan.client.lwm2m.resource.LwM2mObjectInstance;
import leshan.server.lwm2m.observation.ObserveSpec;

public class MandatoryObjectExchange implements LwM2mCreateExchange {

	@Override
	public void respond(final LwM2mResponse response) {}

	@Override
	public byte[] getRequestPayload() {
		return new byte[0];
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
		// TODO Auto-generated method stub

	}

}
