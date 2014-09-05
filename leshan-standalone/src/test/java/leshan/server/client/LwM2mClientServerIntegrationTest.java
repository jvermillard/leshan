package leshan.server.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.operation.ExecuteResponse;
import leshan.client.lwm2m.operation.LwM2mResource;
import leshan.client.lwm2m.operation.ReadResponse;
import leshan.client.lwm2m.operation.WriteResponse;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.resource.LwM2mObjectDefinition;
import leshan.client.lwm2m.resource.Notifier;
import leshan.client.lwm2m.resource.SingleResourceDefinition;
import leshan.server.lwm2m.LwM2mServer;
import leshan.server.lwm2m.bootstrap.BootstrapStoreImpl;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientRegistryImpl;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.CreateRequest;
import leshan.server.lwm2m.message.DeleteRequest;
import leshan.server.lwm2m.message.DiscoverRequest;
import leshan.server.lwm2m.message.ObserveRequest;
import leshan.server.lwm2m.message.ReadRequest;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.observation.ObservationRegistryImpl;
import leshan.server.lwm2m.observation.ResourceObserver;
import leshan.server.lwm2m.security.SecurityRegistry;
import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvType;

import org.junit.After;
import org.junit.Before;
import org.mockito.Matchers;

import ch.ethz.inf.vs.californium.WebLink;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.coap.Response;

public abstract class LwM2mClientServerIntegrationTest {

	protected static final int GOOD_OBJECT_ID = 100;
	protected static final int GOOD_OBJECT_INSTANCE_ID = 0;
	protected static final int FIRST_RESOURCE_ID = 4;
	protected static final int SECOND_RESOURCE_ID = 5;
	protected static final int EXECUTABLE_RESOURCE_ID = 6;

	protected static final int BAD_OBJECT_ID = 1000;
	protected static final String ENDPOINT = "epflwmtm";
	private static final int CLIENT_PORT = 44022;
	private static final String GOOD_PAYLOAD = "1337";
	protected static final int TIMEOUT_MS = 5000;
	private final String clientDataModel = "</lwm2m>;rt=\"oma.lwm2m\", </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";

	protected LwM2mServer server;
	private ClientRegistryImpl clientRegistry;

	protected Map<String,String> clientParameters;

	protected Set<WebLink> objectsAndInstances;
	private InetSocketAddress serverAddress;
	protected LwM2mClient client;
	protected ExecutableResource executableResource;
	protected ValueResource firstResource;
	protected ValueResource secondResource;

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

		executableResource = spy(new ExecutableResource());
		when(executableResource.execute(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt())).thenReturn(ExecuteResponse.success());

		firstResource = new ValueResource();
		secondResource = new ValueResource();

		client = createClient();
	}

	protected LwM2mClient createClient() {
		final LwM2mObjectDefinition objectOne = new LwM2mObjectDefinition(GOOD_OBJECT_ID,
				new SingleResourceDefinition(FIRST_RESOURCE_ID, firstResource),
				new SingleResourceDefinition(SECOND_RESOURCE_ID, secondResource),
				new SingleResourceDefinition(EXECUTABLE_RESOURCE_ID, executableResource));
		return new LwM2mClient(objectOne);
	}

	@After
	public void teardown() {
		client.stop();
		server.stop();
	}

	protected RegisterUplink registerAndGetUplink() {
		final Response goodRawResponse = new Response(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT);
		goodRawResponse.setPayload(GOOD_PAYLOAD);

		final RegisterUplink registerUplink = client.startRegistration(CLIENT_PORT, serverAddress);
		return registerUplink;
	}

	protected void register() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		registerUplink.register(ENDPOINT, clientParameters, TIMEOUT_MS);
	}

	protected Tlv[] createGoodObjectInstaceTlv(final String value0, final String value1) {
		final Tlv[] values = new Tlv[1];
		values[0] = new Tlv(TlvType.OBJECT_INSTANCE, createGoodResourcesTlv(value0, value1), null, 0);
		return values;
	}

	protected Tlv[] createGoodResourcesTlv(final String value0, final String value1) {
		final Tlv[] values = new Tlv[2];
		values[1] = new Tlv(TlvType.RESOURCE_VALUE, null, value0.getBytes(), FIRST_RESOURCE_ID);
		values[0] = new Tlv(TlvType.RESOURCE_VALUE, null, value1.getBytes(), SECOND_RESOURCE_ID);
		return values;
	}

	protected ClientResponse sendRead(final int objectID) {
		return ReadRequest
				.newRequest(getClient(), objectID)
				.send(server.getRequestHandler());
	}

	protected ClientResponse sendRead(final int objectID, final int objectInstanceID) {
		return ReadRequest
				.newRequest(getClient(), objectID, objectInstanceID)
				.send(server.getRequestHandler());
	}

	protected ClientResponse sendRead(final int objectID, final int objectInstanceID, final int resourceID) {
		return ReadRequest
				.newRequest(getClient(), objectID, objectInstanceID, resourceID)
				.send(server.getRequestHandler());
	}

	protected ClientResponse sendObserve(final int objectID, final ResourceObserver observer) {
		return ObserveRequest
				.newRequest(getClient(), observer, objectID)
				.send(server.getRequestHandler());
	}

	protected ClientResponse sendObserve(final int objectID, final int objectInstanceID, final ResourceObserver observer) {
		return ObserveRequest
				.newRequest(getClient(), observer, objectID, objectInstanceID)
				.send(server.getRequestHandler());
	}

	protected ClientResponse sendObserve(final int objectID, final int objectInstanceID, final int resourceID, final ResourceObserver observer) {
		return ObserveRequest
				.newRequest(getClient(), observer, objectID, objectInstanceID, resourceID)
				.send(server.getRequestHandler());
	}

	protected ClientResponse sendDiscover(final int objectID) {
		return DiscoverRequest
				.newRequest(getClient(), objectID)
				.send(server.getRequestHandler());
	}

	protected ClientResponse sendDiscover(final int objectID, final int objectInstanceID) {
		return DiscoverRequest
				.newRequest(getClient(), objectID, objectInstanceID)
				.send(server.getRequestHandler());
	}

	protected ClientResponse sendDiscover(final int objectID, final int objectInstanceID, final int resourceID) {
		return DiscoverRequest
				.newRequest(getClient(), objectID, objectInstanceID, resourceID)
				.send(server.getRequestHandler());
	}

	protected ClientResponse sendCreate(final Tlv[] values, final int objectID) {
		return CreateRequest
				.newRequest(getClient(), objectID, values)
				.send(server.getRequestHandler());
	}

	protected ClientResponse sendDelete(final Tlv[] values, final int objectID, final Integer objectInstanceID) {
		return DeleteRequest
				.newRequest(getClient(), objectID, objectInstanceID)
				.send(server.getRequestHandler());
	}

	protected ClientResponse sendCreate(final Tlv[] values, final int objectID, final int objectInstanceID) {
		return CreateRequest
				.newRequest(getClient(), objectID, objectInstanceID, values)
				.send(server.getRequestHandler());
	}

	protected Client getClient() {
		return clientRegistry.get(ENDPOINT);
	}

	protected void assertResponse(final ClientResponse response, final ResponseCode responseCode, final byte[] payload) {
		assertEquals(responseCode, response.getCode());
		assertEquals(new String(payload), new String(response.getContent()));
	}

	protected void assertEmptyResponse(final ClientResponse response, final ResponseCode responseCode) {
		assertEquals(responseCode, response.getCode());
		final byte[] payload = response.getContent();
		assertTrue(payload == null || payload.length == 0);
	}

	public class ValueResource implements LwM2mResource {

		private String value;
		private Notifier notifier;

		@Override
		public WriteResponse write(final int objectId, final int objectInstanceId, final int resourceId,
				final byte[] valueToWrite) {
			setValue(new String(valueToWrite));

			return WriteResponse.success();
		}

		@Override
		public ReadResponse read() {
			final ReadResponse response = ReadResponse.success(value.getBytes());
			return response;
		}

		public void setValue(final String newValue) {
			value = newValue;
			if(notifier != null){
				notifier.notify(ReadResponse.success(value.getBytes()));
			}
		}

		@Override
		public void observe(final Notifier notifier) {
			this.notifier = notifier;
		}

		@Override
		public ExecuteResponse execute(final int objectId, final int objectInstanceId,
				final int resourceId) {
			return ExecuteResponse.failure();
		}

		@Override
		public boolean isReadable() {
			return true;
		}

	}

	public class ReadWriteListenerWithBrokenWrite implements LwM2mResource {

		private String value;

		@Override
		public WriteResponse write(final int objectId, final int objectInstanceId, final int resourceId,
				final byte[] valueToWrite) {
			if (value == null) {
				value = new String(valueToWrite);
				return WriteResponse.success();
			}
			return WriteResponse.failure();
		}

		@Override
		public ReadResponse read() {
			return ReadResponse.success(value.getBytes());
		}

		@Override
		public void observe(final Notifier notifier) {
			notifier.notify(ReadResponse.success(value.getBytes()));
		}

		@Override
		public ExecuteResponse execute(final int objectId, final int objectInstanceId,
				final int resourceId) {
			return ExecuteResponse.failure();
		}

		@Override
		public boolean isReadable() {
			return true;
		}

	}

	public class ExecutableResource implements LwM2mResource {

		@Override
		public ReadResponse read() {
			return ReadResponse.failure();
		}

		@Override
		public WriteResponse write(final int objectId, final int objectInstanceId,
				final int resourceId, final byte[] valueToWrite) {
			return WriteResponse.failure();
		}

		@Override
		public ExecuteResponse execute(final int objectId, final int objectInstanceId,
				final int resourceId) {
			return ExecuteResponse.success();
		}

		@Override
		public void observe(final Notifier notifier) {
		}

		@Override
		public boolean isReadable() {
			return false;
		}

	}

}
