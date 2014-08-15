package leshan.client.lwm2m;

import leshan.client.lwm2m.response.OperationResponse;

public interface BootstrapDownlink {
	public OperationResponse write();
	public OperationResponse delete();
}
