package leshan.client.request;

import leshan.client.response.ServerResponse;
import leshan.client.server.Server;

public interface LwM2mClientRequest<T extends ServerResponse> {
	/**
	 * Gets the LWM2M Server the request is targeted at.
	 * 
	 * @return the server.
	 */
	public Server getServer();
	

    /**
     * Accept a visitor for this request.
     */
    void accept(LwM2mClientRequestVisitor visitor);

}
