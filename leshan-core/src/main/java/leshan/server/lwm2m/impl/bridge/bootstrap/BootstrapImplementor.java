package leshan.server.lwm2m.impl.bridge.bootstrap;

import leshan.server.lwm2m.bootstrap.BootstrapStore;
import leshan.server.lwm2m.security.SecurityStore;

public interface BootstrapImplementor {
	
	 /** IANA assigned UDP port for CoAP */
    public static final int PORT = 5683;

    /** IANA assigned UDP port for CoAP with DTLS */
    public static final int PORT_DTLS = 5684;
	
	public SecurityStore getSecurityStore();

	public BootstrapStore getBootstrapStore();
	
	public void start();
	
	public void stop();
	
	public void destroy();

}
