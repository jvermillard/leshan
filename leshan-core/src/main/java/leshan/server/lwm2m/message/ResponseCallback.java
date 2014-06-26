package leshan.server.lwm2m.message;

/**
 * A callback to be notified of client responses.
 */
public interface ResponseCallback {

    /**
     * A response from the remote client
     * 
     * @param response the response
     */
    void onResponse(ClientResponse response);

    /**
     * No response received and all retransmissions failed.
     */
    void onTimeout();

    /**
     * No response received and the client rejected the request.
     */
    void onReject();

    /**
     * No response received and the request was cancelled.
     */
    void onCancel();
}
