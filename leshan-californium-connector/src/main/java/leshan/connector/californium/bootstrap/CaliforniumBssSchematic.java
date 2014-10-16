package leshan.connector.californium.bootstrap;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import leshan.connector.californium.resource.CaliforniumCoapResourceProxy;
import leshan.connector.californium.server.CaliforniumLwM2mRequestSender;
import leshan.connector.californium.server.CaliforniumPskStore;
import leshan.connector.californium.server.CaliforniumServerImplementor;
import leshan.server.lwm2m.bootstrap.BootstrapStore;
import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.impl.bridge.server.CoapServerImplementorSchematic;
import leshan.server.lwm2m.impl.security.SecureEndpoint;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.resource.BootstrapResource;
import leshan.server.lwm2m.security.SecurityRegistry;

import org.apache.commons.lang.Validate;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.scandium.DTLSConnector;

public class CaliforniumBssSchematic implements CoapServerImplementorSchematic<CaliforniumServerImplementor, CaliforniumCoapResourceProxy>{

	private final Set<InetSocketAddress> enpointAddress = new HashSet<InetSocketAddress>();
	private final Set<InetSocketAddress> secureEndpointAddress = new HashSet<InetSocketAddress>();
	private ClientRegistry clientRegistry;
	private SecurityRegistry securityRegistry;
	private ObservationRegistry observationRegistry;
	private CaliforniumCoapResourceProxy coapResourceProxy;
	private CoapServer coapServer;
	private CaliforniumLwM2mRequestSender requestSender;
	//TODO This currently isn't created or set anywhere.
	private BootstrapStore boostrapStore;
	
	@Override
	public void addEndpoint(final InetSocketAddress localAddress) {
		Validate.notNull(localAddress, "IP address cannot be null");
		enpointAddress.add(localAddress);
		
	}

	@Override
	public void addSecureEndpoint(final InetSocketAddress localSecureAddress) {
		Validate.notNull(localSecureAddress, "IP address cannot be null");
		secureEndpointAddress.add(localSecureAddress);
		
	}

	@Override
	public void bindResource(final BootstrapResource coapResourceProxy) {
		this.coapResourceProxy = coapResourceProxy;
		
	}

	@Override
	public void setClientRegistry(final ClientRegistry clientRegistry) {
		this.clientRegistry = clientRegistry;
		
	}

	@Override
	public void setSecurityRegistry(final SecurityRegistry securityRegistry) {
		this.securityRegistry = securityRegistry;
		
	}

	@Override
	public void setObservationRegistry(final ObservationRegistry observationRegistry) {
		this.observationRegistry = observationRegistry;
		
	}

	@Override
	public CaliforniumServerImplementor buildCoapServerImplementor() {
		coapServer = new CoapServer();
		
		final Set<Endpoint> endpoints = new HashSet<Endpoint>();
		for(final InetSocketAddress address : enpointAddress) {
			final Endpoint endpoint = new CoAPEndpoint(address);
			coapServer.addEndpoint(endpoint);
			endpoints.add(endpoint);	
		}
		for(final InetSocketAddress address : secureEndpointAddress) {
			final DTLSConnector connector = new DTLSConnector(address, null);
	        connector.getConfig().setPskStore(new CaliforniumPskStore(this.securityRegistry, this.clientRegistry));
	
	        final Endpoint secureEndpoint = new SecureEndpoint(connector);
	        coapServer.addEndpoint(secureEndpoint);
	        endpoints.add(secureEndpoint);
		}
		
		requestSender = new CaliforniumLwM2mRequestSender(endpoints, observationRegistry);
		
		final BootstrapResource bsResource = new BootstrapResource(boostrapStore, coapResourceProxy);
		coapServer.add(coapResourceProxy.getCoapResource());
		
		return new CaliforniumServerImplementor(coapServer, requestSender, null, null, securityRegistry);
	}

}
