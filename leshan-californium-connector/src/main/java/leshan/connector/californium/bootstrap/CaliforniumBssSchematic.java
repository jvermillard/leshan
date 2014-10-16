package leshan.connector.californium.bootstrap;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.scandium.DTLSConnector;

import leshan.connector.californium.resource.CaliforniumCoapResourceProxy;
import leshan.connector.californium.server.CaliforniumLwM2mRequestSender;
import leshan.connector.californium.server.CaliforniumPskStore;
import leshan.connector.californium.server.CaliforniumServerImplementor;
import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.impl.bridge.server.CoapServerImplementorSchematic;
import leshan.server.lwm2m.impl.californium.RegisterResource;
import leshan.server.lwm2m.impl.security.SecureEndpoint;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.security.SecurityRegistry;

public class CaliforniumBssSchematic implements CoapServerImplementorSchematic<CaliforniumServerImplementor, BootstrapResource>{

	private final Set<InetSocketAddress> enpointAddress = new HashSet<InetSocketAddress>();
	private final Set<InetSocketAddress> secureEndpointAddress = new HashSet<InetSocketAddress>();
	private ClientRegistry clientRegistry;
	private SecurityRegistry securityRegistry;
	private ObservationRegistry observationRegistry;
	private BootstrapResource coapResourceProxy;
	private CoapServer coapServer;
	private CaliforniumLwM2mRequestSender requestSender;
	
	@Override
	public void addEndpoint(InetSocketAddress localAddress) {
		Validate.notNull(localAddress, "IP address cannot be null");
		enpointAddress.add(localAddress);
		
	}

	@Override
	public void addSecureEndpoint(InetSocketAddress localSecureAddress) {
		Validate.notNull(localSecureAddress, "IP address cannot be null");
		secureEndpointAddress.add(localSecureAddress);
		
	}

	@Override
	public void bindResource(BootstrapResource coapResourceProxy) {
		this.coapResourceProxy = coapResourceProxy;
		
	}

	@Override
	public void setClientRegistry(ClientRegistry clientRegistry) {
		this.clientRegistry = clientRegistry;
		
	}

	@Override
	public void setSecurityRegistry(SecurityRegistry securityRegistry) {
		this.securityRegistry = securityRegistry;
		
	}

	@Override
	public void setObservationRegistry(ObservationRegistry observationRegistry) {
		this.observationRegistry = observationRegistry;
		
	}

	@Override
	public CaliforniumServerImplementor buildCoapServerImplementor() {
		coapServer = new CoapServer();
		
		final Set<Endpoint> endpoints = new HashSet<Endpoint>();
		for(InetSocketAddress address : enpointAddress) {
			Endpoint endpoint = new CoAPEndpoint(address);
			coapServer.addEndpoint(endpoint);
			endpoints.add(endpoint);	
		}
		for(InetSocketAddress address : secureEndpointAddress) {
			final DTLSConnector connector = new DTLSConnector(address, null);
	        connector.getConfig().setPskStore(new CaliforniumPskStore(this.securityRegistry, this.clientRegistry));
	
	        final Endpoint secureEndpoint = new SecureEndpoint(connector);
	        coapServer.addEndpoint(secureEndpoint);
	        endpoints.add(secureEndpoint);
		}
		
		requestSender = new CaliforniumLwM2mRequestSender(endpoints, observationRegistry);
		
		final RegisterResource rdResource = new RegisterResource(clientRegistry, securityRegistry, coapResourceProxy);
		coapServer.add(coapResourceProxy.getCoapResource());
		
		return new CaliforniumServerImplementor(coapServer, requestSender);
	}

}
