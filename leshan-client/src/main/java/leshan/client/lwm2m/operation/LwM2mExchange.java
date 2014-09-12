package leshan.client.lwm2m.operation;

import leshan.server.lwm2m.observation.ObserveSpec;

public interface LwM2mExchange {

	public void respond(LwM2mResponse response);
	public byte[] getRequestPayload();

	public boolean hasObjectInstanceId();
	public int getObjectInstanceId();
	public boolean isObserve();
	public ObserveSpec getObserveSpec();

}
