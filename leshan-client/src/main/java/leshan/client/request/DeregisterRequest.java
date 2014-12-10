package leshan.client.request;

import leshan.client.request.identifier.ClientIdentifier;


public class DeregisterRequest extends AbstractRegisteredLwM2mClientRequest {

	public DeregisterRequest(final ClientIdentifier clientIdentifier) {
		super(clientIdentifier);
	}

	public DeregisterRequest(final ClientIdentifier clientIdentifier,
			final long timeout) {
		super(clientIdentifier, timeout);
	}

	@Override
	public void accept(final LwM2mClientRequestVisitor visitor) {
		visitor.visit(this);
	}

}
