package leshan.client.lwm2m.resource;

public interface ExecuteListener {

	public static final ExecuteListener DUMMY = new DummyListener();

	public void execute(int objectId, int objectInstanceId, int resourceId);

	static class DummyListener implements ExecuteListener {

		@Override
		public void execute(final int objectId, final int objectInstanceId, final int resourceId) {
		}

	}

}
