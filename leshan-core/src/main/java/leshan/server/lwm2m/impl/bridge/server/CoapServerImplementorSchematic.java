package leshan.server.lwm2m.impl.bridge.server;

import java.net.InetSocketAddress;

import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.resource.proxy.CoapResourceProxy;
import leshan.server.lwm2m.security.SecurityRegistry;

public interface CoapServerImplementorSchematic<E extends CoapServerImplementor, R extends CoapResourceProxy> {
	
	public CoapServerImplementorSchematic<E, R> addEndpoint(InetSocketAddress... localaddress);
	
	public CoapServerImplementorSchematic<E, R> addSecureEndpoint(InetSocketAddress... localSecureAddress);

	public CoapServerImplementorSchematic<E, R> bindResource(R coapResourceProxy);
	
	public CoapServerImplementorSchematic<E, R> setClientRegistry(ClientRegistry clientRegistry);
	
	public CoapServerImplementorSchematic<E, R> setSecurityRegistry(SecurityRegistry registry);
	
	public CoapServerImplementorSchematic<E, R> setObservationRegistry(ObservationRegistry observationRegistry);
	
	public E buildCoapServerImplementor();
}
