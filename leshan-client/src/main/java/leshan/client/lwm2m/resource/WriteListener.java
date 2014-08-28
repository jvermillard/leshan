package leshan.client.lwm2m.resource;

public interface WriteListener {
	
	public static final WriteListener DUMMY = new DummyListener();

	void write(int objectId, int objectInstanceId,
			int resourceId, byte[] valueToWrite);

	static class DummyListener implements WriteListener {

		@Override
		public void write(final int objectId, final int objectInstanceId, final int resourceId,
				final byte[] valueToWrite) {
		}

	}
}
