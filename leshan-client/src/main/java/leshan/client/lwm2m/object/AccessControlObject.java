package leshan.client.lwm2m.object;

public class AccessControlObject {

	private LWM2MServer accessControlOwner;
	private int objectId;
	private int objectInstanceId;

	private AccessControlObject() {}

	public static AccessControlObject instantiate(final LWM2MServer server) {
		return new AccessControlObject();
	}
}