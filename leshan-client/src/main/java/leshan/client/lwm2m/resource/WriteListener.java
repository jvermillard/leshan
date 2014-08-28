package leshan.client.lwm2m.resource;

public interface WriteListener {

	public static final WriteListener DUMMY = new DummyListener();

	public WriteResponse write(int objectId, int objectInstanceId,
			int resourceId, byte[] valueToWrite);

	static class DummyListener implements WriteListener {

		@Override
		public WriteResponse write(final int objectId, final int objectInstanceId, final int resourceId,
				final byte[] valueToWrite) {
			return WriteResponse.failure();
		}

	}
}
