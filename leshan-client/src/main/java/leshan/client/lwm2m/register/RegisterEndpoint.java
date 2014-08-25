package leshan.client.lwm2m.register;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import leshan.client.lwm2m.request.Request;

public class RegisterEndpoint {
	private final Map<String, String> queryString;
	private final InetSocketAddress destination;
	
	public RegisterEndpoint(final InetSocketAddress destination, final Map<String, String> queryString) {
		this.destination = destination;
		this.queryString = new HashMap<>(queryString);
	}
	
	@Override
	public String toString() {
		return destination.getHostString() + ":" + destination.getPort() + "/rd?" + Request.toQueryStringMap(queryString);
	}
}
