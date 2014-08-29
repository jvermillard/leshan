package leshan.client.lwm2m.operation;

public interface Writable {

	public WriteResponse write(int objectId, int objectInstanceId,
			int resourceId, byte[] valueToWrite);
	
	public static final Writable NOT_WRITABLE = new Writable(){

		@Override
		public WriteResponse write(final int objectId, final int objectInstanceId,
				final int resourceId, final byte[] valueToWrite) {
			return WriteResponse.failure();
		}
	};
}
