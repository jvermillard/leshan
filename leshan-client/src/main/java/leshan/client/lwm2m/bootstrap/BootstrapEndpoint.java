package leshan.client.lwm2m.bootstrap;

import java.util.HashMap;
import java.util.Map;

import leshan.client.lwm2m.request.Request;

public class BootstrapEndpoint {
	private final Map<String, String> queryString;
	
	public BootstrapEndpoint(final Map<String, String> queryString) {
		this.queryString = new HashMap<>(queryString);
	}
	
	@Override
	public String toString() {
		return "/bs?" + Request.toQueryStringMap(queryString);
	}
}