package leshan.client.request;

import java.net.InetSocketAddress;

public interface LwM2mClientRequest {
    /**
     * Accept a visitor for this request.
     */
    void accept(LwM2mClientRequestVisitor visitor);


    /**
     * An optional address of the Endpoint that should be used in this Request.
     * @return NULL if the default should be used, otherwise the Address matching the Endpoint.
     */
	public InetSocketAddress getClientEndpointAddress();

}
