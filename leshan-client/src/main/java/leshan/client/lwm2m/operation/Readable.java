package leshan.client.lwm2m.operation;

import leshan.client.lwm2m.resource.Notifier;

public interface Readable {

	ReadResponse read();
	
	void observe(Notifier notifier);
	
	public static final Readable NOT_READABLE = new Readable(){
		
		@Override
		public ReadResponse read() {
			return ReadResponse.failure();
		}

		@Override
		public void observe(final Notifier notifier) {
		}
		
	};


}
