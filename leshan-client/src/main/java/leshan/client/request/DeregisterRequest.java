package leshan.client.request;

import leshan.client.server.Server;

public class DeregisterRequest implements LwM2mClientRequest{

	private final Server server;

	public DeregisterRequest(final Server server) {
		this.server = server;
	}
	
	@Override
	public Server getServer() {
		return server;
	}

	@Override
	public void accept(final LwM2mClientRequestVisitor visitor) {
		visitor.visit(this);
	}

}
