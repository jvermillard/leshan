package leshan.client.request;


public interface LwM2mClientRequest {
    /**
     * Accept a visitor for this request.
     */
    void accept(LwM2mClientRequestVisitor visitor);

}
