package leshan.server.lwm2m.resource.proxy;

import java.net.InetSocketAddress;

import leshan.server.lwm2m.client.BindingMode;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.LinkObject;


public abstract class ExchangeProxy {

	public abstract RequestProxy getRequest();
	
	public abstract void respondWithCreated();

	public abstract void respondWithBadRequest(String... message);

	public abstract InetSocketAddress getEndpointAddress();

	public abstract boolean isUsingSecureEndpoint();

	public abstract String getPskIdentity();

	public abstract void killTlsSession();

	public abstract Client createNewClient(String registrationId, String endpoint,
			String lwVersion, Long lifetime, String smsNumber, BindingMode binding,
			LinkObject[] objectLinks, InetSocketAddress registrationEndpoint);

	public abstract void setLocationPath(String locationPath);
}
