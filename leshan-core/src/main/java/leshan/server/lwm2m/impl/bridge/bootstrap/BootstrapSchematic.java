package leshan.server.lwm2m.impl.bridge.bootstrap;

import java.net.InetSocketAddress;

import leshan.server.lwm2m.bootstrap.BootstrapStore;
import leshan.server.lwm2m.resource.proxy.CoapResourceProxy;
import leshan.server.lwm2m.security.SecurityStore;

public interface BootstrapSchematic<E extends BootstrapImplementor, R extends CoapResourceProxy> {
	
	public BootstrapSchematic<E, R> addEndpoint(InetSocketAddress... localaddress);
	
	public BootstrapSchematic<E, R> addSecureEndpoint(InetSocketAddress... localSecureAddress);
	
	public BootstrapSchematic<E, R> setSecurityStore(SecurityStore securityStore);
	
	public BootstrapSchematic<E, R> setBootstrapStore(BootstrapStore bootstrapStore);
	
	public BootstrapSchematic<E, R> bindResource(R coapResourceProxy);
	
	public E build();

}
