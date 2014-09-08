package leshan.client.lwm2m.operation;

public interface LwM2mExchange {

	public void respond(LwM2mResponse response);
	public byte[] getRequestPayload();

	public boolean hasObjectInstanceId();
	public int getObjectInstanceId();

}
