package leshan.server.lwm2m.resource.proxy;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

import leshan.server.lwm2m.client.BindingMode;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientUpdate;
import leshan.server.lwm2m.client.LinkObject;
import leshan.server.lwm2m.request.CoapResponseCode.ResponseCode;


public abstract class ExchangeProxy {

	public abstract RequestProxy getRequest();

	public abstract InetSocketAddress getEndpointAddress();

	public abstract boolean isUsingSecureEndpoint();

	public abstract String getPskIdentity();

	public abstract void killTlsSession();

	public abstract Client createNewClient(String registrationId, String endpoint,
			String lwVersion, Long lifetime, String smsNumber, BindingMode binding,
			LinkObject[] objectLinks, InetSocketAddress registrationEndpoint);

	public abstract void setLocationPath(String locationPath);

	public abstract List<String> getURIPaths();

	public abstract void respond(ResponseCode code, final String... errorMessage);

	public abstract ClientUpdate createNewClientUpdate(String registrationId, Long lifetime,
			String smsNumber, BindingMode binding, LinkObject[] objectLinks);

	public abstract List<String> getUQRIQueries();

	public abstract RequestProxy createDeleteAllRequest();

	public abstract RequestProxy createPostSecurityRequest(ByteBuffer encoded);

	public abstract RequestProxy createPostServerRequest(ByteBuffer encoded);

}
