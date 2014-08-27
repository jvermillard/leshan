package leshan.server.stuff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.manage.ManageDownlink;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.server.lwm2m.LwM2mServer;
import leshan.server.lwm2m.bootstrap.BootstrapStoreImpl;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientRegistryImpl;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ReadRequest;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.observation.ObservationRegistryImpl;
import leshan.server.lwm2m.security.SecurityRegistry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.ethz.inf.vs.californium.WebLink;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.coap.Response;

public class Stuff {

	private static final int CLIENT_PORT = 44022;
	private static final String GOOD_PAYLOAD = "1337";
	protected static final int TIMEOUT_MS = 5000;
	private static final String LWM2M_SERVER_ADDRESS = "coap://localhost:5683/rd?ep=device1";

	private final String VALID_REQUEST_PAYLOAD = "</lwm2m>;rt=\"oma.lwm2m\", </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";
	private final String clientDataModel = "</lwm2m>;rt=\"oma.lwm2m\", </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";

	private LwM2mServer server;
	private ClientRegistryImpl clientRegistry;

	private Map<String,String> clientParameters;

	private Set<WebLink> objectsAndInstances;
	private InetSocketAddress serverAddress;

	@Before
	public void setup() {
		clientParameters = new HashMap<>();
		objectsAndInstances = LinkFormat.parse(clientDataModel);
		
		serverAddress = new InetSocketAddress(5683);
		final InetSocketAddress serverAddressSecure = new InetSocketAddress(5684);
		clientRegistry = new ClientRegistryImpl();
		final ObservationRegistry observationRegistry = new ObservationRegistryImpl();
		final SecurityRegistry securityRegistry = new SecurityRegistry();
		final BootstrapStoreImpl bsStore = new BootstrapStoreImpl();
		server = new LwM2mServer(serverAddress, serverAddressSecure, clientRegistry, securityRegistry, observationRegistry, bsStore);
		server.start();
	}

	@After
	public void teardown() {
		server.stop();
	}

	@Test
	public void registeredDeviceCanHaveReadSentToIt() {
		final LwM2mClient client = new LwM2mClient();

		final ManageDownlink downlink = mock(ManageDownlink.class);
		final Response goodRawResponse = new Response(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT);
		goodRawResponse.setPayload(GOOD_PAYLOAD);
		final OperationResponse goodResponse = OperationResponse.of(goodRawResponse);
		Mockito.when(downlink.read(Mockito.anyInt())).thenReturn(goodResponse);
		
		final RegisterUplink registerUplink = client.startRegistration(CLIENT_PORT, serverAddress, downlink);
		final OperationResponse register = registerUplink.register("device1", clientParameters, objectsAndInstances, TIMEOUT_MS);
		
		Assert.assertTrue(register.isSuccess());

		final Client registeredClient = clientRegistry.get("device1");
		assertNotNull(registeredClient);
		final ClientResponse response = ReadRequest.newRequest(registeredClient, 1).send(server.getRequestHandler());

		assertEquals(ResponseCode.CONTENT, response.getCode());
		assertEquals(GOOD_PAYLOAD, new String(response.getContent()));

		client.stop();
	}

}
