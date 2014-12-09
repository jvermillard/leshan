package leshan.client.request;


public class DeregisterRequest extends AbstractLwM2mClientRequest {

	private final String clientLocation;
	public DeregisterRequest(final String clientLocation) {
		this.clientLocation = clientLocation;
	}
	
	public String getClientLocation() {
		return clientLocation;
	}
	

	@Override
	public void accept(final LwM2mClientRequestVisitor visitor) {
		visitor.visit(this);
	}

}
