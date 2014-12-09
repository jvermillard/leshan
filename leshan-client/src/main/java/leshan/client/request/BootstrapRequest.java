package leshan.client.request;

public class BootstrapRequest extends AbstractLwM2mClientRequest{
	private final String clientEndpointIdentifier;

	public BootstrapRequest(final String clientEndpointIdentifier){
		this.clientEndpointIdentifier = clientEndpointIdentifier;
	}
	
	@Override
	public void accept(final LwM2mClientRequestVisitor visitor) {
		visitor.visit(this);
	}
	
	public String getClientEndpointIdentifier() {
		return clientEndpointIdentifier;
	}

}
