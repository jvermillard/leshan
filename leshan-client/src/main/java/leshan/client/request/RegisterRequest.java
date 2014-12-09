package leshan.client.request;

import leshan.LinkObject;
import leshan.client.server.Server;

public class RegisterRequest implements LwM2mClientRequest{
	private final Server server;
	private LinkObject objectModel;

	public RegisterRequest(final Server server){
		this.server = server;
	}

	@Override
	public Server getServer() {
		return server;
	}

	public LinkObject getObjectModel() {
		return objectModel;
	}
	
	public void setObjectModel(final LinkObject objectModel) {
		this.objectModel = objectModel;
	}

	@Override
	public void accept(final LwM2mClientRequestVisitor visitor) {
		visitor.visit(this);
	}

}
