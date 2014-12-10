package leshan.client.californium.impl;

import leshan.client.request.identifier.ClientIdentifier;

import org.eclipse.californium.core.coap.Request;

public class CaliforniumClientIdentifier implements ClientIdentifier {

	private final String location;
	private final String endpointIdentifier;

	public CaliforniumClientIdentifier(final String location, final String endpointIdentifier) {
		this.location = location;
		this.endpointIdentifier = endpointIdentifier;
	}

	public String getLocation() {
		return location;
	}

	public String getEndpointIdentifier() {
		return endpointIdentifier;
	}

	@Override
	public void accept(final Request coapRequest) {
		final String[] locationPaths = location.split("/");
		for(final String location : locationPaths){
			if(location.length() != 0){
				coapRequest.getOptions().addURIPath(location);
			}
		}
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ClientIdentifier[" + getEndpointIdentifier() + "|" + getLocation() + "]");

		return builder.toString();
	}
}
