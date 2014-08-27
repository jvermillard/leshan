package leshan.server.stuff;

import static org.junit.Assert.assertArrayEquals;
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
import leshan.client.lwm2m.resource.ClientObject;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.client.lwm2m.util.ResponseCallback;
import leshan.server.lwm2m.LwM2mServer;
import leshan.server.lwm2m.bootstrap.BootstrapStoreImpl;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientRegistryImpl;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.CreateRequest;
import leshan.server.lwm2m.message.ReadRequest;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.observation.ObservationRegistryImpl;
import leshan.server.lwm2m.security.SecurityRegistry;
import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.ethz.inf.vs.californium.WebLink;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.coap.Response;

import com.jayway.awaitility.Awaitility;

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
	private LwM2mClient client;

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

		final ClientObject obj1 = new ClientObject();
		client = new LwM2mClient(obj1);
	}

	@After
	public void teardown() {
		client.stop();
		server.stop();
	}

	@Test
	public void registeredDeviceCanHaveReadSentToIt() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		final OperationResponse register = registerUplink.register("device1", clientParameters, TIMEOUT_MS);

		Assert.assertTrue(register.isSuccess());

		final Client registeredClient = clientRegistry.get("device1");
		assertNotNull(registeredClient);
		final ClientResponse response = ReadRequest.newRequest(registeredClient, 1).send(server.getRequestHandler());

		assertResponse(response, ResponseCode.CONTENT, new byte[0]);
	}

	@Test
	public void registeredDeviceCanHaveReadSentToItAsync() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		final ResponseCallback callback = new ResponseCallback();
		registerUplink.register("device1", clientParameters, objectsAndInstances, callback);

		Awaitility.await().untilTrue(callback.isCalled());

		Assert.assertTrue(callback.isSuccess());

		final Client registeredClient = clientRegistry.get("device1");
		assertNotNull(registeredClient);
		final ClientResponse response = ReadRequest.newRequest(registeredClient, 1).send(server.getRequestHandler());

		assertResponse(response, ResponseCode.CONTENT, new byte[0]);
	}

	@Test
	public void canCreateInstanceOfObject() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		registerUplink.register("device1", clientParameters, TIMEOUT_MS);

		final Tlv[] values = new Tlv[2];
		values[0] = new Tlv(TlvType.RESOURCE_VALUE, null, "hello".getBytes(), 0);
		values[1] = new Tlv(TlvType.RESOURCE_VALUE, null, "goodbye".getBytes(), 1);
		final ClientResponse response = CreateRequest.newRequest(clientRegistry.get("device1"), 1, values).send(server.getRequestHandler());

		assertEquals(ResponseCode.CREATED, response.getCode());
	}

	private RegisterUplink registerAndGetUplink() {
		final ManageDownlink downlink = mock(ManageDownlink.class);
		final Response goodRawResponse = new Response(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT);
		goodRawResponse.setPayload(GOOD_PAYLOAD);
		final OperationResponse goodResponse = OperationResponse.of(goodRawResponse);
		Mockito.when(downlink.read(Mockito.anyInt())).thenReturn(goodResponse);

		final RegisterUplink registerUplink = client.startRegistration(CLIENT_PORT, serverAddress, downlink);
		return registerUplink;
	}

	private void assertResponse(final ClientResponse response, final ResponseCode responseCode, final byte[] payload) {
		assertEquals(responseCode, response.getCode());
		assertArrayEquals(payload, response.getContent());
	}

}
