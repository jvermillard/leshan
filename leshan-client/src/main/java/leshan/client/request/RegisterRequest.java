package leshan.client.request;

import java.util.Map;

public class RegisterRequest extends AbstractLwM2mClientRequest implements LwM2mContentRequest, LwM2mIdentifierRequest{
	private final Map<String, String> clientParameters;
	private final String clientEndpointIdentifier;

	public RegisterRequest(final String clientEndpointIdentifier, final Map<String, String> clientParameters){
		this.clientEndpointIdentifier = clientEndpointIdentifier;
		this.clientParameters = clientParameters;
	}
	
	@Override
	public void accept(final LwM2mClientRequestVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public Map<String, String> getClientParameters() {
		return clientParameters;
	}
	
	@Override
	public String getClientEndpointIdentifier() {
		return clientEndpointIdentifier;
	}

}
