package leshan.client.lwm2m.operation;

public interface Executable {


	public ExecuteResponse execute(int objectId, int objectInstanceId, int resourceId);

	public static final Executable NOT_EXECUTABLE = new Executable(){

		@Override
		public ExecuteResponse execute(final int objectId, final int objectInstanceId, final int resourceId) {
			return ExecuteResponse.failure();
		}

	};

}
