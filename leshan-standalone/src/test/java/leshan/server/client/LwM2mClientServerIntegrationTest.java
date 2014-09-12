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
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.resource.IntegerLwM2mExchange;
import leshan.client.lwm2m.resource.IntegerLwM2mResource;
import leshan.client.lwm2m.resource.LwM2mObjectDefinition;
import leshan.client.lwm2m.resource.MultipleLwM2mExchange;
import leshan.client.lwm2m.resource.MultipleLwM2mResource;
import leshan.client.lwm2m.resource.SingleResourceDefinition;
import leshan.client.lwm2m.resource.StringLwM2mExchange;
import leshan.client.lwm2m.resource.StringLwM2mResource;
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
import leshan.server.lwm2m.message.WriteRequest;
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

	protected static final int MULTIPLE_OBJECT_ID = GOOD_OBJECT_ID + 2;
	protected static final int MULTIPLE_RESOURCE_ID = 0;

	protected static final int INT_OBJECT_ID = GOOD_OBJECT_ID + 3;
	protected static final int INT_RESOURCE_ID = 0;

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
	protected MultipleResource multipleResource;
	protected IntValueResource intResource;

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


		firstResource = new ValueResource();
		secondResource = new ValueResource();
		executableResource = spy(new ExecutableResource());
		multipleResource = new MultipleResource();
		intResource = new IntValueResource();

		client = createClient();
	}

	protected LwM2mClient createClient() {
		final ReadWriteListenerWithBrokenWrite brokenResourceListener = new ReadWriteListenerWithBrokenWrite();

		final boolean required = true;
		final boolean writable = true;

		final LwM2mObjectDefinition objectOne = new LwM2mObjectDefinition(GOOD_OBJECT_ID,
				new SingleResourceDefinition(FIRST_RESOURCE_ID, firstResource, required, writable),
				new SingleResourceDefinition(SECOND_RESOURCE_ID, secondResource, required, writable),
				new SingleResourceDefinition(EXECUTABLE_RESOURCE_ID, executableResource, !required, !writable));
		final LwM2mObjectDefinition objectTwo = new LwM2mObjectDefinition(BROKEN_OBJECT_ID,
				new SingleResourceDefinition(BROKEN_RESOURCE_ID, brokenResourceListener, required, writable));
		final LwM2mObjectDefinition objectThree = new LwM2mObjectDefinition(MULTIPLE_OBJECT_ID,
				new SingleResourceDefinition(MULTIPLE_RESOURCE_ID, multipleResource, !required, !writable));
		final LwM2mObjectDefinition objectFour = new LwM2mObjectDefinition(INT_OBJECT_ID,
				new SingleResourceDefinition(INT_RESOURCE_ID, intResource, !required, !writable));
		return new LwM2mClient(objectOne, objectTwo, objectThree, objectFour);
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

	protected static Tlv[] createGoodResourcesTlv(final String value0, final String value1) {
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

	// objectInstanceId is an Integer, because null is a valid value
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

	protected ClientResponse sendUpdate(final Tlv[] payload, final int objectId, final int objectInstanceId, final int resourceId) {
		return WriteRequest
				.newUpdateRequest(getClient(), objectId, objectInstanceId, resourceId, payload)
				.send(server.getRequestHandler());
	}

	protected ClientResponse sendReplace(final Tlv[] payload, final int objectId, final int objectInstanceId, final int resourceId) {
		return WriteRequest
				.newReplaceRequest(getClient(), objectId, objectInstanceId, resourceId, payload)
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
			assertEquals("Expected TLVs " + Arrays.toString(expected) + ", but was " + Arrays.toString(actual),
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

	public class ValueResource extends StringLwM2mResource {

		private String value = "blergs";

		public void setValue(final String newValue) {
			value = newValue;
			notifyResourceUpdated();
		}

		public String getValue() {
			return value;
		}

		@Override
		public void handleWrite(final StringLwM2mExchange exchange) {
			setValue(exchange.getRequestPayload());

			exchange.respondSuccess();
		}

		@Override
		public void handleRead(final StringLwM2mExchange exchange) {
			exchange.respondContent(value);
		}

	}

	public class IntValueResource extends IntegerLwM2mResource {

		private int value = 0;

		public void setValue(final int newValue) {
			value = newValue;
			notifyResourceUpdated();
		}

		public int getValue() {
			return value;
		}

		@Override
		public void handleWrite(final IntegerLwM2mExchange exchange) {
			setValue(exchange.getRequestPayload());

			exchange.respondSuccess();
		}

		@Override
		public void handleRead(final IntegerLwM2mExchange exchange) {
			exchange.respondContent(value);
		}

	}

	public class ReadWriteListenerWithBrokenWrite extends StringLwM2mResource {

		private String value;

		@Override
		public void handleWrite(final StringLwM2mExchange exchange) {
			if (value == null) {
				value = exchange.getRequestPayload();
				exchange.respondSuccess();
			} else {
				exchange.respondFailure();
			}
		}

		@Override
		public void handleRead(final StringLwM2mExchange exchange) {
			exchange.respondContent(value);
		}

	}

	public class ExecutableResource extends StringLwM2mResource {

		@Override
		public void handleExecute(final LwM2mExchange exchange) {
			exchange.respond(ExecuteResponse.success());
		}

	}

	public class MultipleResource extends MultipleLwM2mResource {

		private Map<Integer, byte[]> value;

		public void setValue(final Map<Integer, byte[]> initialValue) {
			this.value = initialValue;
		}

		@Override
		public void handleRead(final MultipleLwM2mExchange exchange) {
			exchange.respondContent(value);
		}

		@Override
		public void handleWrite(final MultipleLwM2mExchange exchange) {
			this.value = exchange.getRequestPayload();
			exchange.respondSuccess();
		}

	}

}
