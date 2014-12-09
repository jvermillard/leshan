package leshan.client.request;

import java.net.InetSocketAddress;

public abstract class AbstractRegisteredLwM2mClientRequest extends
		AbstractLwM2mClientRequest {

	protected final String clientLocation;

	public AbstractRegisteredLwM2mClientRequest(final String clientLocation, final InetSocketAddress clientEndpointAddress, final long timeout) {
		super(clientEndpointAddress, timeout);
		this.clientLocation = clientLocation;
	}

	public AbstractRegisteredLwM2mClientRequest(final String clientLocation, final long timeout) {
		this(clientLocation, null, timeout);
	}

	public AbstractRegisteredLwM2mClientRequest(final String clientLocation, final InetSocketAddress clientEndpointAddress) {
		this(clientLocation, clientEndpointAddress, DEFAULT_TIMEOUT_MS);
	}

	public AbstractRegisteredLwM2mClientRequest(final String clientLocation) {
		this(clientLocation, null);
	}

	public String getClientLocation() {
		return clientLocation;
	}

}