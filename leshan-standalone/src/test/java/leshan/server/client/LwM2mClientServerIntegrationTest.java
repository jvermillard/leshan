package leshan.server.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.operation.ExecuteResponse;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.ReadResponse;
import leshan.client.lwm2m.operation.WriteResponse;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.resource.LwM2mObjectDefinition;
import leshan.client.lwm2m.resource.LwM2mResource;
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
import leshan.server.lwm2m.tlv.TlvDecoder;
import leshan.server.lwm2m.tlv.TlvType;

import org.junit.After;
import org.junit.Before;

import ch.ethz.inf.vs.californium.WebLink;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.coap.Response;

public abstract class LwM2mClientServerIntegrationTest {

	protected static final int GOOD_OBJECT_ID = 100;
	protected static final int GOOD_OBJECT_INSTANCE_ID = 0;
	protected static final int FIRST_RESOURCE_ID = 4;
	protected static final int SECOND_RESOURCE_ID = 5;
	protected static final int EXECUTABLE_RESOURCE_ID = 6;
	protected static final int INVALID_RESOURCE_ID = 9;

	protected static final int BROKEN_OBJECT_ID = GOOD_OBJECT_ID + 1;
	protected static final int BROKEN_RESOURCE_ID = 7;

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

		firstResource = new ValueResource();
		secondResource = new ValueResource();

		client = createClient();
	}

	protected LwM2mClient createClient() {
		final ReadWriteListenerWithBrokenWrite brokenResourceListener = new ReadWriteListenerWithBrokenWrite();

		final LwM2mObjectDefinition objectOne = new LwM2mObjectDefinition(GOOD_OBJECT_ID,
				new SingleResourceDefinition(FIRST_RESOURCE_ID, firstResource, true, true),
				new SingleResourceDefinition(SECOND_RESOURCE_ID, secondResource, true, true),
				new SingleResourceDefinition(EXECUTABLE_RESOURCE_ID, executableResource, false, false));
		final LwM2mObjectDefinition objectTwo = new LwM2mObjectDefinition(BROKEN_OBJECT_ID,
				new SingleResourceDefinition(BROKEN_RESOURCE_ID, brokenResourceListener, true, true));
		return new LwM2mClient(objectOne, objectTwo);
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
		values[0] = new Tlv(TlvType.RESOURCE_VALUE, null, value0.getBytes(), FIRST_RESOURCE_ID);
		values[1] = new Tlv(TlvType.RESOURCE_VALUE, null, value1.getBytes(), SECOND_RESOURCE_ID);
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
		try {
			final Tlv[] expected = TlvDecoder.decode(ByteBuffer.wrap(payload));
			final Tlv[] actual = TlvDecoder.decode(ByteBuffer.wrap(response.getContent()));
			assertEquals("Expected TLVs " + Arrays.toString(expected) + ", but was " + Arrays.toString(actual) + "",
					new String(payload), new String(response.getContent()));
		} catch (final Exception e) {
			assertEquals(new String(payload), new String(response.getContent()));
		}
	}

	protected void assertEmptyResponse(final ClientResponse response, final ResponseCode responseCode) {
		assertEquals(responseCode, response.getCode());
		final byte[] payload = response.getContent();
		assertTrue(payload == null || payload.length == 0);
	}

	public class ValueResource implements LwM2mResource {

		private String value = "blergs";
		private Notifier notifier;

		public void setValue(final String newValue) {
			value = newValue;
			if(notifier != null){
				notifier.notify(ReadResponse.success(value.getBytes()));
			}
		}

		public String getValue() {
			return value;
		}

		@Override
		public void write(final LwM2mExchange exchange) {
			setValue(new String(exchange.getRequestPayload()));

			exchange.respond(WriteResponse.success());
		}

		@Override
		public void read(final LwM2mExchange exchange) {
			exchange.respond(ReadResponse.success(value.getBytes()));
		}

		@Override
		public void observe(final Notifier notifier) {
			this.notifier = notifier;
		}

		@Override
		public void execute(final LwM2mExchange exchange) {
			exchange.respond(ExecuteResponse.failure());
		}

		@Override
		public boolean isReadable() {
			return true;
		}

	}

	public class ReadWriteListenerWithBrokenWrite implements LwM2mResource {

		private String value;

		@Override
		public void write(final LwM2mExchange exchange) {
			if (value == null) {
				value = new String(exchange.getRequestPayload());
				exchange.respond(WriteResponse.success());
			}
			exchange.respond(WriteResponse.failure());
		}

		@Override
		public void read(final LwM2mExchange exchange) {
			exchange.respond(ReadResponse.success(value.getBytes()));
		}

		@Override
		public void observe(final Notifier notifier) {
			notifier.notify(ReadResponse.success(value.getBytes()));
		}

		@Override
		public void execute(final LwM2mExchange exchange) {
			exchange.respond(ExecuteResponse.failure());
		}

		@Override
		public boolean isReadable() {
			return true;
		}

	}

	public class ExecutableResource implements LwM2mResource {

		@Override
		public void read(final LwM2mExchange exchange) {
			exchange.respond(ReadResponse.failure());
		}

		@Override
		public void write(final LwM2mExchange exchange) {
			exchange.respond(WriteResponse.notAllowed());
		}

		@Override
		public void execute(final LwM2mExchange exchange) {
			exchange.respond(ExecuteResponse.success());
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
