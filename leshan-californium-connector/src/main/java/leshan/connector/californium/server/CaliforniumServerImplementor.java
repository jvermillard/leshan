package leshan.connector.californium.server;

import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.impl.bridge.server.CoapServerImplementor;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.request.LwM2mRequestSender;
import leshan.server.lwm2m.security.SecurityRegistry;

import org.eclipse.californium.core.CoapServer;

public class CaliforniumServerImplementor implements CoapServerImplementor {

	private final CoapServer coapServer;
	private final CaliforniumLwM2mRequestSender requestSender;
	private final ClientRegistry clientRegistry;
	private final ObservationRegistry observationRegistry;
	private final SecurityRegistry securityRegistry;
	
	public CaliforniumServerImplementor(CoapServer coapServer, CaliforniumLwM2mRequestSender requestSender, 
										ClientRegistry clientRegistry, ObservationRegistry observationRegistry, 
										SecurityRegistry securityRegistry) {
		this.coapServer = coapServer;
		this.requestSender = requestSender;
		this.clientRegistry = clientRegistry;
		this.observationRegistry = observationRegistry;
		this.securityRegistry = securityRegistry;
	}

	@Override
	public LwM2mRequestSender getLWM2MRequestSender() {
		return requestSender;
	}

	@Override
	public void start() {
		coapServer.start();
		
	}

	@Override
	public void stop() {
		coapServer.stop();
		
	}

	@Override
	public void destroy() {
		coapServer.destroy();
		
	}

	@Override
	public ClientRegistry getClientRegistry() {
		return clientRegistry;
	}

	@Override
	public ObservationRegistry getObservationRegistry() {
		return observationRegistry;
	}

	@Override
	public SecurityRegistry getSecurityRegistry() {
		return securityRegistry;
	}

}
