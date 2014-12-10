package leshan.client.request;


public abstract class AbstractRegisteredLwM2mClientRequest extends
		AbstractLwM2mClientRequest {

	protected final String clientLocation;

	public AbstractRegisteredLwM2mClientRequest(final String clientLocation, final long timeout) {
		super(timeout);
		this.clientLocation = clientLocation;
	}

	public AbstractRegisteredLwM2mClientRequest(final String clientLocation) {
		this(clientLocation, DEFAULT_TIMEOUT_MS);
	}

	public String getClientLocation() {
		return clientLocation;
	}

}