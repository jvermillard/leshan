package leshan.client.request;

import java.net.InetSocketAddress;


public class DeregisterRequest extends AbstractRegisteredLwM2mClientRequest {

	public DeregisterRequest(final String clientLocation) {
		super(clientLocation);
	}

	public DeregisterRequest(final String clientLocation,
			final InetSocketAddress clientEndpointAddress, final long timeout) {
		super(clientLocation, clientEndpointAddress, timeout);
	}

	public DeregisterRequest(final String clientLocation,
			final InetSocketAddress clientEndpointAddress) {
		super(clientLocation, clientEndpointAddress);
	}

	public DeregisterRequest(final String clientLocation, final long timeout) {
		super(clientLocation, timeout);
	}

	@Override
	public void accept(final LwM2mClientRequestVisitor visitor) {
		visitor.visit(this);
	}

}
