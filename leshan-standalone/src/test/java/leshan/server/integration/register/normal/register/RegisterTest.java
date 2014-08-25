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
import leshan.client.lwm2m.response.OperationResponse;
import leshan.server.LeshanMain;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.inf.vs.californium.WebLink;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.LinkFormat;

public class RegisterTest {

	private static final int TIMEOUT_MS = 5000;
	private LeshanMain server;
	private InetSocketAddress serverAddress;
	private RegisterDownlink downlink;
	private String clientEndpoint;
	private Map<String, String> clientParameters;
	private Set<WebLink> objectsAndInstances;
	private int clientPort;
	private final String clientDataModel = "</lwm2m>;rt=\"oma.lwm2m\", </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";

	@Before
	public void setUp() throws UnknownHostException{
        server = new LeshanMain();
        server.start();
        serverAddress = new InetSocketAddress(InetAddress.getLocalHost(), 5683);
        clientPort = 9000;
        clientEndpoint = UUID.randomUUID().toString();
        clientParameters = new HashMap<>();
        objectsAndInstances = LinkFormat.parse(clientDataModel);
	}
	
	@After
	public void tearDown(){
        server.stop();
	}
	
	@Test
	public void testRegisterAndDeregister() {
		final ClientFactory clientFactory = new ClientFactory();
		
		final RegisterUplink registerUplink = clientFactory.startRegistration(clientPort, serverAddress, downlink);
		
		OperationResponse response = registerUplink.register(clientEndpoint, clientParameters, objectsAndInstances, TIMEOUT_MS);
		
		assertTrue(response.isSuccess());
		assertEquals(response.getResponseCode(), ResponseCode.CREATED);
		final String payload = new String(response.getPayload());
		System.out.println("REGISTER: " + response + " with payload: " + payload);
		
		
		response = registerUplink.deregister(clientEndpoint);
		
		assertTrue(response.isSuccess());
		assertEquals(response.getResponseCode(), ResponseCode.DELETED);
		System.out.println("DEREGISTER: " + response + " with payload: " + payload);
		
	}

}
