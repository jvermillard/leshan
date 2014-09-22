package leshan.client.lwm2m.resource;

import org.eclipse.californium.core.CoapResource;

public abstract class ClientResourceBase extends CoapResource implements LinkFormattable, ClientObservable{
	protected static final int IS_OBSERVE = 0;

	public ClientResourceBase(final Integer id) {
		super(Integer.toString(id));
		setObservable(true);
	}
	
	@Override
	public void notifyObserverRelations(){
		System.out.println("Getting Notified!");
		notifyObserverRelations();
	}

}
