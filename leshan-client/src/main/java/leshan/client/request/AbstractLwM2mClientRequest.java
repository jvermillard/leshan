package leshan.client.request;

import java.net.InetSocketAddress;

public abstract class AbstractLwM2mClientRequest implements LwM2mClientRequest {
	private static final long DEFAULT_TIMEOUT_MS = 500;
	
	private final InetSocketAddress clientEndpointAddress;
	private final long timeout;

	public AbstractLwM2mClientRequest() {
		this(null, DEFAULT_TIMEOUT_MS);
	}
	
	public AbstractLwM2mClientRequest(final long timeout) {
		this(null, timeout);
	}

	public AbstractLwM2mClientRequest(final InetSocketAddress clientEndpointAddress){
		this(clientEndpointAddress, DEFAULT_TIMEOUT_MS);
	}

	public AbstractLwM2mClientRequest(final InetSocketAddress clientEndpointAddress,
			final long timeout) {
		this.clientEndpointAddress = clientEndpointAddress;
		this.timeout = timeout;
	}

	@Override
	public InetSocketAddress getClientEndpointAddress() {
		return clientEndpointAddress;
	}
	
	public long getTimeout() {
		return timeout;
	}
}