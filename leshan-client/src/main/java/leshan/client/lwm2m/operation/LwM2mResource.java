package leshan.client.lwm2m.operation;

import leshan.client.lwm2m.resource.Notifier;

public interface LwM2mResource {

	public ReadResponse read();

	public WriteResponse write(int objectId, int objectInstanceId, int resourceId, byte[] valueToWrite);

	public ExecuteResponse execute(int objectId, int objectInstanceId, int resourceId);

	void observe(Notifier notifier);

	boolean isReadable();

}
