package leshan.server.lwm2m.impl.bridge.server;

import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.request.LwM2mRequestSender;
import leshan.server.lwm2m.security.SecurityRegistry;

public interface CoapServerImplementor {
	
	 /** IANA assigned UDP port for CoAP */
    public static final int PORT = 5683;

    /** IANA assigned UDP port for CoAP with DTLS */
    public static final int PORT_DTLS = 5684;
	
	public void createCoAPServer(ClientRegistry clientRegistry, ObservationRegistry observationRegistry, SecurityRegistry securityRegistry);
	
	public void bindEndpoints();
		
	public void bindResource();
	
	public LwM2mRequestSender getLWM2MRequestSender();
	
	public void start();
	
	public void stop();

	public void destroy();

}
