package leshan.client.lwm2m.operation;

public interface Readable {

	ReadResponse read();
	
	
	public static final Readable NOT_READABLE = new Readable(){
		
		@Override
		public ReadResponse read() {
			return ReadResponse.failure();
		}
		
	};

}
