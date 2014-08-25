package leshan.client.lwm2m.register;

import java.net.InetSocketAddress;

public class RegisteredEndpoint {
	private final String endpointName;
	private final InetSocketAddress destination;
	
	public RegisteredEndpoint(final InetSocketAddress destination, final String endpointName) {
		this.destination = destination;
		this.endpointName = endpointName;
	}

	@Override
	public String toString() {
		return destination.getHostString() + ":" + destination.getPort() + endpointName;
	}

}
