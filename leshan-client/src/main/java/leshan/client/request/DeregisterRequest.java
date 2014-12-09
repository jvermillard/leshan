package leshan.client.request;

import leshan.client.server.Server;

public class DeregisterRequest implements LwM2mClientRequest{

	private final Server server;
	private final String clientLocation;

	public DeregisterRequest(final Server server, final String clientLocation) {
		this.server = server;
		this.clientLocation = clientLocation;
	}
	
	@Override
	public Server getServer() {
		return server;
	}
	
	public String getClientLocation() {
		return clientLocation;
	}

	@Override
	public void accept(final LwM2mClientRequestVisitor visitor) {
		visitor.visit(this);
	}

}
