package leshan.client.lwm2m.object;

public class AccessControlObject {

	private AccessControlObject() {}

	public static AccessControlObject instantiate(final LWM2MServer server) {
		return new AccessControlObject();
	}
}