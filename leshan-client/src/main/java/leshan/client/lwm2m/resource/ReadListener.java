package leshan.client.lwm2m.resource;

public interface ReadListener {

	public static final ReadListener DUMMY = new ReadListener(){

		@Override
		public byte[] read() {
			return new byte[0];
		}
		
	};

	byte[] read();
	

}
