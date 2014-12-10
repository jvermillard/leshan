package leshan.client.request;


public class DeregisterRequest extends AbstractRegisteredLwM2mClientRequest {

	public DeregisterRequest(final String clientLocation) {
		super(clientLocation);
	}

	public DeregisterRequest(final String clientLocation,
			final long timeout) {
		super(clientLocation, timeout);
	}

	@Override
	public void accept(final LwM2mClientRequestVisitor visitor) {
		visitor.visit(this);
	}

}
