package leshan.client.request;

import leshan.client.response.OperationResponse;
import leshan.client.util.ResponseCallback;

public interface LwM2mClientRequestSender {
    OperationResponse send(LwM2mClientRequest request);

    void send(LwM2mClientRequest request, ResponseCallback responseCallback);
}
