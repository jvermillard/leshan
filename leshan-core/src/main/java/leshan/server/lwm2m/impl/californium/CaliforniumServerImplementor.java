package leshan.server.lwm2m.impl.californium;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.scandium.DTLSConnector;

import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.impl.bridge.server.CoapServerImplementor;
import leshan.server.lwm2m.impl.security.SecureEndpoint;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.request.LwM2mRequestSender;
import leshan.server.lwm2m.security.SecurityRegistry;

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

	public CaliforniumServerImplementor(InetSocketAddress localAddress,	InetSocketAddress secureLocalAddress) {
        Validate.notNull(localAddress, "IP address cannot be null");
        Validate.notNull(secureLocalAddress, "Secure IP address cannot be null");
		this.localAddress = localAddress;
		this.secureLocalAddress = secureLocalAddress;
	}

	@Override
	public void createCoAPServer(ClientRegistry clientRegistry, ObservationRegistry observationRegistry, SecurityRegistry securityRegistry) {
        coapServer = new CoapServer();		
        this.clientRegistry = clientRegistry;
        this.observationRegistry  = observationRegistry;
        this.securityRegistry = securityRegistry;
	}

	@Override
	public void bindEndpoints() {
		Set<Endpoint> endpoints = new HashSet<Endpoint>();
		Endpoint endpoint = new CoAPEndpoint(localAddress);
		endpoints.add(endpoint);
		
		if(secureLocalAddress != null) {
	        DTLSConnector connector = new DTLSConnector(secureLocalAddress, null);
	        connector.getConfig().setPskStore(new CaliforniumPskStore(this.securityRegistry, this.clientRegistry));
	
	        Endpoint secureEndpoint = new SecureEndpoint(connector);
	        coapServer.addEndpoint(secureEndpoint);
	        endpoints.add(endpoint);
		}
		requestSender = new CaliforniumLwM2mRequestSender(endpoints, observationRegistry);
	}

	@Override
	public void bindResource() {
		RegisterResource rdResource = new RegisterResource(clientRegistry, securityRegistry);
		coapServer.add(rdResource);
		
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
