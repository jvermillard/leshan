package leshan.client.lwm2m.bootstrap;

import leshan.client.lwm2m.response.OperationResponse;

public interface BootstrapDownlink {
	public OperationResponse write(int objectId, int objectInstanceId, int resourceId);
	public OperationResponse delete();
}
