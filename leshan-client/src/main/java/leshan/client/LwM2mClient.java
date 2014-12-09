package leshan.client;

import leshan.LinkObject;
import leshan.client.request.LwM2mClientRequest;
import leshan.client.response.OperationResponse;
import leshan.client.util.ResponseCallback;

public interface LwM2mClient {

	public void start();

	public void stop();

	public OperationResponse send(LwM2mClientRequest request);

	public void send(LwM2mClientRequest request, ResponseCallback callback);

	public LinkObject[] getObjectModel(Integer... ids);

}