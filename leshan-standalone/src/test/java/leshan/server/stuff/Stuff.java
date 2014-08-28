package leshan.server.stuff;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.manage.ManageDownlink;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.resource.ClientObject;
import leshan.client.lwm2m.resource.ExecutableResourceDefinition;
import leshan.client.lwm2m.resource.ExecuteListener;
import leshan.client.lwm2m.resource.StringResourceDefinition;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.client.lwm2m.util.ResponseCallback;
import leshan.server.lwm2m.LwM2mServer;
import leshan.server.lwm2m.bootstrap.BootstrapStoreImpl;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientRegistryImpl;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.CreateRequest;
import leshan.server.lwm2m.message.ExecRequest;
import leshan.server.lwm2m.message.ReadRequest;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.message.WriteRequest;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.observation.ObservationRegistryImpl;
import leshan.server.lwm2m.security.SecurityRegistry;
import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvEncoder;
import leshan.server.lwm2m.tlv.TlvType;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import ch.ethz.inf.vs.californium.WebLink;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.coap.Response;

public class Stuff {

	private static final int GOOD_OBJECT_ID = 100;
	private static final int GOOD_OBJECT_INSTANCE_ID = 0;
	private static final int FIRST_RESOURCE_ID = 4;
	private static final int SECOND_RESOURCE_ID = 5;
	private static final int EXECUTABLE_RESOURCE_ID = 6;

	private static final int BAD_OBJECT_ID = 1000;
	private static final String ENDPOINT = "epflwmtm";
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
	private ExecuteListener executeListener;

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

		executeListener = mock(ExecuteListener.class);
		final ClientObject objectOne = new ClientObject(GOOD_OBJECT_ID,
				new StringResourceDefinition(FIRST_RESOURCE_ID),
				new StringResourceDefinition(SECOND_RESOURCE_ID),
				new ExecutableResourceDefinition(EXECUTABLE_RESOURCE_ID, executeListener));
		final ClientObject objectTwo = new ClientObject(GOOD_OBJECT_ID + 1,
				new StringResourceDefinition(0));
		client = new LwM2mClient(objectOne, objectTwo);
	}

	@After
	public void teardown() {
		client.stop();
		server.stop();
	}

	@Test
	public void registeredDeviceExists() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		final OperationResponse register = registerUplink.register(ENDPOINT, clientParameters, TIMEOUT_MS);

		assertTrue(register.isSuccess());
		assertNotNull(getClient());
	}

	@Test(expected=IllegalArgumentException.class)
	public void failToCreateClientWithNull(){
		client = new LwM2mClient((ClientObject[])null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void failToCreateClientWithSameObjectTwice(){
		final ClientObject objectOne = new ClientObject(1);
		client = new LwM2mClient(objectOne, objectOne);
	}

	@Test
	public void registeredDeviceExistsAsync() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		final ResponseCallback callback = new ResponseCallback();
		registerUplink.register(ENDPOINT, clientParameters, objectsAndInstances, callback);

		await().untilTrue(callback.isCalled());

		assertTrue(callback.isSuccess());
		assertNotNull(getClient());
	}

	@Test
	public void canReadObject() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		registerUplink.register(ENDPOINT, clientParameters, TIMEOUT_MS);
		assertResponse(sendGet(GOOD_OBJECT_ID), ResponseCode.CONTENT, new byte[0]);
	}

	@Test
	public void canCreateInstanceOfObject() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		registerUplink.register(ENDPOINT, clientParameters, TIMEOUT_MS);

		final ClientResponse response = sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);
		assertResponse(response, ResponseCode.CREATED, ("/" + GOOD_OBJECT_ID + "/0").getBytes());
	}

	@Test
	public void canCreateMultipleInstanceOfObject() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		registerUplink.register(ENDPOINT, clientParameters, TIMEOUT_MS);

		final ClientResponse response = sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);
		assertResponse(response, ResponseCode.CREATED, ("/" + GOOD_OBJECT_ID + "/0").getBytes());

		final ClientResponse responseTwo = sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);
		assertResponse(responseTwo, ResponseCode.CREATED, ("/" + GOOD_OBJECT_ID + "/1").getBytes());
	}

	@Test
	public void canNotCreateInstanceOfObject() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		registerUplink.register(ENDPOINT, clientParameters, TIMEOUT_MS);

		final ClientResponse response = sendCreate(createResourcesTlv("hello", "goodbye"), BAD_OBJECT_ID);
		assertResponse(response, ResponseCode.NOT_FOUND, null);
	}

	@Test
	public void objectCreationIsReflectedInObjectRead() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		registerUplink.register(ENDPOINT, clientParameters, TIMEOUT_MS);

		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		assertResponse(sendGet(GOOD_OBJECT_ID), ResponseCode.CONTENT, TlvEncoder.encode(createObjectInstaceTlv("hello", "goodbye")).array());
	}

	@Test
	public void canReadObjectInstace() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		registerUplink.register(ENDPOINT, clientParameters, TIMEOUT_MS);

		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		assertResponse(sendGet(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.CONTENT, TlvEncoder.encode(createResourcesTlv("hello", "goodbye")).array());
	}

	@Test
	public void canReadResource() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		registerUplink.register(ENDPOINT, clientParameters, TIMEOUT_MS);

		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		assertResponse(sendGet(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID),
				ResponseCode.CONTENT, "hello".getBytes());
		assertResponse(sendGet(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
				ResponseCode.CONTENT, "goodbye".getBytes());
	}

	@Test
	public void canWriteReplaceToResource() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		registerUplink.register(ENDPOINT, clientParameters, TIMEOUT_MS);

		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse response = WriteRequest.newReplaceRequest(getClient(), GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID,
				"world", ContentFormat.TEXT).send(server.getRequestHandler());

		assertResponse(response, ResponseCode.CHANGED, new byte[0]);
		assertResponse(sendGet(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
				ResponseCode.CONTENT, "world".getBytes());
	}

	@Test
	public void canWritePartialUpdateToResource() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		registerUplink.register(ENDPOINT, clientParameters, TIMEOUT_MS);

		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse response = WriteRequest.newUpdateRequest(getClient(), GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID,
				"world", ContentFormat.TEXT).send(server.getRequestHandler());

		assertResponse(response, ResponseCode.CHANGED, new byte[0]);
		assertResponse(sendGet(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
				ResponseCode.CONTENT, "world".getBytes());
	}

	@Ignore
	@Test
	public void canNotExecuteWriteOnlyResource() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		registerUplink.register(ENDPOINT, clientParameters, TIMEOUT_MS);

		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse response = ExecRequest.newRequest(getClient(), GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID,
				"world", ContentFormat.TEXT).send(server.getRequestHandler());

		assertResponse(response, ResponseCode.METHOD_NOT_ALLOWED, new byte[0]);
	}

	@Test
	public void canExecuteResource() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		registerUplink.register(ENDPOINT, clientParameters, TIMEOUT_MS);

		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse response = ExecRequest.newRequest(getClient(), GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, EXECUTABLE_RESOURCE_ID,
				"world", ContentFormat.TEXT).send(server.getRequestHandler());

		assertResponse(response, ResponseCode.CHANGED, new byte[0]);
		verify(executeListener).execute();
	}

	private RegisterUplink registerAndGetUplink() {
		final ManageDownlink downlink = mock(ManageDownlink.class);
		final Response goodRawResponse = new Response(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT);
		goodRawResponse.setPayload(GOOD_PAYLOAD);
		final OperationResponse goodResponse = OperationResponse.of(goodRawResponse);
		when(downlink.read(Mockito.anyInt())).thenReturn(goodResponse);

		final RegisterUplink registerUplink = client.startRegistration(CLIENT_PORT, serverAddress, downlink);
		return registerUplink;
	}

	private Tlv[] createObjectInstaceTlv(final String value0, final String value1) {
		final Tlv[] values = new Tlv[1];
		values[0] = new Tlv(TlvType.OBJECT_INSTANCE, createResourcesTlv(value0, value1), null, 0);
		return values;
	}

	private Tlv[] createResourcesTlv(final String value0, final String value1) {
		final Tlv[] values = new Tlv[2];
		values[1] = new Tlv(TlvType.RESOURCE_VALUE, null, value0.getBytes(), FIRST_RESOURCE_ID);
		values[0] = new Tlv(TlvType.RESOURCE_VALUE, null, value1.getBytes(), SECOND_RESOURCE_ID);
		return values;
	}

	private ClientResponse sendGet(final int objectID) {
		return ReadRequest
				.newRequest(getClient(), objectID)
				.send(server.getRequestHandler());
	}

	private ClientResponse sendGet(final int objectID, final int objectInstanceID) {
		return ReadRequest
				.newRequest(getClient(), objectID, objectInstanceID)
				.send(server.getRequestHandler());
	}

	private ClientResponse sendGet(final int objectID, final int objectInstanceID, final int resourceID) {
		return ReadRequest
				.newRequest(getClient(), objectID, objectInstanceID, resourceID)
				.send(server.getRequestHandler());
	}

	private ClientResponse sendCreate(final Tlv[] values, final int objectID) {
		return CreateRequest
				.newRequest(getClient(), objectID, values)
				.send(server.getRequestHandler());
	}

	private Client getClient() {
		return clientRegistry.get(ENDPOINT);
	}

	private void assertResponse(final ClientResponse response, final ResponseCode responseCode, final byte[] payload) {
		assertEquals(responseCode, response.getCode());
		assertArrayEquals(payload, response.getContent());
	}

}
