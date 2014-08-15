package leshan.client.lwm2m;

import leshan.client.lwm2m.response.OperationResponse;

public interface RegisterUplink {
	public OperationResponse register();
	public OperationResponse update();
	public OperationResponse deregister();
	//...
	public OperationResponse notify(String todo);

}
