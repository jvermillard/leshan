package leshan.client.lwm2m.register;

public class DeregisterEndpoint {
	private final String endpointName;
	
	public DeregisterEndpoint(final String endpointName) {
		this.endpointName = endpointName;
	}

	@Override
	public String toString() {
		return "/rd/" + endpointName;
	}

}
