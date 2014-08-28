package leshan.client.lwm2m.resource;

public interface ExecuteListener {

	public void execute(int objectId, int objectInstanceId, int resourceId);

}
