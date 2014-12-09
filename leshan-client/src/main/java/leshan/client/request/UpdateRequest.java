package leshan.client.request;

import java.net.InetSocketAddress;
import java.util.Map;

public class UpdateRequest extends AbstractRegisteredLwM2mClientRequest implements LwM2mContentRequest{

	private final Map<String, String> updatedParameters;

	public UpdateRequest(final String clientLocation, final Map<String, String> updatedParameters) {
		super(clientLocation);
		this.updatedParameters = updatedParameters;
	}

	public UpdateRequest(final String clientLocation,
			final InetSocketAddress clientEndpointAddress, final long timeout, final Map<String, String> updatedParameters) {
		super(clientLocation, clientEndpointAddress, timeout);
		this.updatedParameters = updatedParameters;
	}

	public UpdateRequest(final String clientLocation,
			final InetSocketAddress clientEndpointAddress, final Map<String, String> updatedParameters) {
		super(clientLocation, clientEndpointAddress);
		this.updatedParameters = updatedParameters;
	}

	public UpdateRequest(final String clientLocation, final long timeout, final Map<String, String> updatedParameters) {
		super(clientLocation, timeout);
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

}
