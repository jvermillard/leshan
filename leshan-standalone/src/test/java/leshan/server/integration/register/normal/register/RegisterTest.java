package leshan.server.integration.register.normal.register;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import leshan.client.lwm2m.Uplink;
import leshan.client.lwm2m.factory.ClientFactory;
import leshan.client.lwm2m.register.RegisterDownlink;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.response.Callback;
import leshan.server.LeshanMain;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.inf.vs.californium.WebLink;

public class RegisterTest {

	private static final int TIMEOUT_MS = 5000;
	private LeshanMain server;
	private InetSocketAddress serverAddress;
	private RegisterDownlink downlink;
	private String clientEndpoint;
	private Map<String, String> clientParameters;
	private Set<WebLink> objectsAndInstances;
	private int clientPort;

	@Before
	public void setUp() throws UnknownHostException{
        server = new LeshanMain();
        server.start();
        serverAddress = new InetSocketAddress(InetAddress.getLocalHost(), 5683);
        clientPort = 9000;
        clientEndpoint = UUID.randomUUID().toString();
        clientParameters = new HashMap<>();
        objectsAndInstances = new HashSet<>();
	}
	
	@After
	public void tearDown(){
        server.stop();
	}
	
	@Test
	public void testRegisterAndDeregister() {
		final ClientFactory clientFactory = new ClientFactory();
		final RegisterUplink registerUplink = clientFactory.startRegistration(clientPort, serverAddress, downlink);
		registerUplink.register(clientEndpoint, clientParameters, objectsAndInstances, TIMEOUT_MS);
	}

}
