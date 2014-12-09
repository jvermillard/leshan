package leshan.client.request;

import java.util.Map;

import leshan.LinkObject;
import leshan.client.server.Server;

public class RegisterRequest implements LwM2mClientRequest{
	private final Server server;
	private LinkObject objectModel;
	private final Map<String, String> clientParameters;

	public RegisterRequest(final Server server, final Map<String, String> clientParameters){
		this.server = server;
		this.clientParameters = clientParameters;
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
	
	public Map<String, String> getClientParameters() {
		return clientParameters;
	}

}
