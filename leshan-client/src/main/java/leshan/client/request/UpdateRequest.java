package leshan.client.request;

import java.util.Map;

import leshan.client.request.identifier.ClientIdentifier;

public class UpdateRequest extends AbstractRegisteredLwM2mClientRequest implements LwM2mContentRequest{

	private final Map<String, String> updatedParameters;

	public UpdateRequest(final ClientIdentifier clientIdentifier, final Map<String, String> updatedParameters) {
		super(clientIdentifier);
		this.updatedParameters = updatedParameters;
	}

	public UpdateRequest(final ClientIdentifier clientIdentifier, final long timeout, final Map<String, String> updatedParameters) {
		super(clientIdentifier, timeout);
		this.updatedParameters = updatedParameters;
	}
	
	@Override
	public Map<String, String> getClientParameters() {
		return updatedParameters;
	}

	@Override
	public void accept(final LwM2mClientRequestVisitor visitor) {
		visitor.visit(this);
	}

	public String getClientEndpointIdentifier() {
		return null;
	}

}
