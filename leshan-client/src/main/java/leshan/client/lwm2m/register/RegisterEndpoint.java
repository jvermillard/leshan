package leshan.client.lwm2m.register;

import java.util.HashMap;
import java.util.Map;

import leshan.client.lwm2m.request.Request;

public class RegisterEndpoint {
	private final Map<String, String> queryString;
	
	public RegisterEndpoint(final Map<String, String> queryString) {
		this.queryString = new HashMap<>(queryString);
	}
	
	@Override
	public String toString() {
		return "/rd?" + Request.toQueryStringMap(queryString);
	}
}
