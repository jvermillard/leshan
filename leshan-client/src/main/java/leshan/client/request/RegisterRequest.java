package leshan.client.request;

import java.util.Map;

import leshan.LinkObject;

public class RegisterRequest extends AbstractLwM2mClientRequest{
	private LinkObject objectModel;
	private final Map<String, String> clientParameters;
	private final String clientEndpointIdentifier;

	public RegisterRequest(final String clientEndpointIdentifier, final Map<String, String> clientParameters){
		this.clientEndpointIdentifier = clientEndpointIdentifier;
		this.clientParameters = clientParameters;
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
	
	public String getClientEndpointIdentifier() {
		return clientEndpointIdentifier;
	}

}
