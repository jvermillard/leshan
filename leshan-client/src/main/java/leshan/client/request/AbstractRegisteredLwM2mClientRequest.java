package leshan.client.request;

import leshan.client.request.identifier.ClientIdentifier;


public abstract class AbstractRegisteredLwM2mClientRequest extends
		AbstractLwM2mClientRequest {

	protected final ClientIdentifier clientIdentifier;

	public AbstractRegisteredLwM2mClientRequest(final ClientIdentifier clientIdentifier, final long timeout) {
		super(timeout);
		this.clientIdentifier = clientIdentifier;
	}

	public AbstractRegisteredLwM2mClientRequest(final ClientIdentifier clientIdentifier) {
		this(clientIdentifier, DEFAULT_TIMEOUT_MS);
	}

	public ClientIdentifier getClientIdentifier() {
		return clientIdentifier;
	}

}