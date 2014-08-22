package leshan.client.lwm2m.register;

public class RegisteredEndpoint {
	private final String endpointName;
	
	public RegisteredEndpoint(final String endpointName) {
		this.endpointName = endpointName;
	}

	@Override
	public String toString() {
		return "/rd/" + endpointName;
	}

}
