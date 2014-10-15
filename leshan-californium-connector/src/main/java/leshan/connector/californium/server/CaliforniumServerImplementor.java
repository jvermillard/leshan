package leshan.connector.californium.server;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.impl.bridge.server.CoapServerImplementor;
import leshan.server.lwm2m.impl.californium.RegisterResource;
import leshan.server.lwm2m.impl.security.SecureEndpoint;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.request.LwM2mRequestSender;
import leshan.server.lwm2m.resource.californium.CaliforniumCoapResourceProxy;
import leshan.server.lwm2m.security.SecurityRegistry;

import org.apache.commons.lang.Validate;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.scandium.DTLSConnector;

public class CaliforniumServerImplementor implements CoapServerImplementor {

	private final InetSocketAddress localAddress;
	private final InetSocketAddress secureLocalAddress;
	private CoapServer coapServer;
	private ClientRegistry clientRegistry;
	private ObservationRegistry observationRegistry;
	private SecurityRegistry securityRegistry;
	private CaliforniumLwM2mRequestSender requestSender;
	
	public CaliforniumServerImplementor() {
		this(new InetSocketAddress(PORT), new InetSocketAddress(PORT_DTLS));
	}

	public CaliforniumServerImplementor(final InetSocketAddress localAddress,	final InetSocketAddress secureLocalAddress) {
        Validate.notNull(localAddress, "IP address cannot be null");
        Validate.notNull(secureLocalAddress, "Secure IP address cannot be null");
		this.localAddress = localAddress;
		this.secureLocalAddress = secureLocalAddress;
	}

	@Override
	public void createCoAPServer(final ClientRegistry clientRegistry, final ObservationRegistry observationRegistry, final SecurityRegistry securityRegistry) {
        coapServer = new CoapServer();		
        this.clientRegistry = clientRegistry;
        this.observationRegistry  = observationRegistry;
        this.securityRegistry = securityRegistry;
	}

	@Override
	public void bindEndpoints() {
		final Set<Endpoint> endpoints = new HashSet<Endpoint>();
		final Endpoint endpoint = new CoAPEndpoint(localAddress);
		endpoints.add(endpoint);
		
		if(secureLocalAddress != null) {
	        final DTLSConnector connector = new DTLSConnector(secureLocalAddress, null);
	        connector.getConfig().setPskStore(new CaliforniumPskStore(this.securityRegistry, this.clientRegistry));
	
	        final Endpoint secureEndpoint = new SecureEndpoint(connector);
	        coapServer.addEndpoint(secureEndpoint);
	        endpoints.add(endpoint);
		}
		requestSender = new CaliforniumLwM2mRequestSender(endpoints, observationRegistry);
	}

	@Override
	public void bindResource() {
		final CaliforniumCoapResourceProxy rdResourceProxy = new CaliforniumCoapResourceProxy();
		final RegisterResource rdResource = new RegisterResource(clientRegistry, securityRegistry, rdResourceProxy);
		coapServer.add(rdResourceProxy.getCoapResource());
		
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

}
