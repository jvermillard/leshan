package leshan.client.lwm2m;

import java.util.HashMap;
import java.util.Map;

public class RegistrationEndpoint {
	private final Map<String, String> queryString;
	
	public RegistrationEndpoint(final Map<String, String> queryString) {
		this.queryString = new HashMap<>(queryString);
	}
	
	@Override
	public String toString() {
		return "/rd?" + Request.toQueryStringMap(queryString);
	}
}
