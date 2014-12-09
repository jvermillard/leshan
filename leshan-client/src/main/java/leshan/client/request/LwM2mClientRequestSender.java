package leshan.client.request;

import leshan.client.response.ServerResponse;
import leshan.client.util.ResponseCallback;

public interface LwM2mClientRequestSender {
    <T extends ServerResponse> T send(LwM2mClientRequest<T> request);

    <T extends ServerResponse> void send(LwM2mClientRequest<T> request, ResponseCallback<T> responseCallback);
}
