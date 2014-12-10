package leshan.client.request;


public abstract class AbstractLwM2mClientRequest implements LwM2mClientRequest {
	protected static final long DEFAULT_TIMEOUT_MS = 500;
	
	private final long timeout;

	public AbstractLwM2mClientRequest() {
		this(DEFAULT_TIMEOUT_MS);
	}
	
	public AbstractLwM2mClientRequest(final long timeout) {
		this.timeout = timeout;
	}

	public final long getTimeout() {
		return timeout;
	}
}