package leshan.server.stuff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.net.InetSocketAddress;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.manage.ManageDownlink;
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
import org.junit.Before;
import org.junit.Test;

public class Stuff {

	private static final String LWM2M_SERVER_ADDRESS = "coap://localhost:5683/rd?ep=device1";

	private final String VALID_REQUEST_PAYLOAD = "</lwm2m>;rt=\"oma.lwm2m\", </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";

	private LwM2mServer server;
	private ClientRegistryImpl clientRegistry;

	@Before
	public void setup() {
		final InetSocketAddress serverAddress = new InetSocketAddress(5683);
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

		client.startRegistration(44022, new InetSocketAddress("localhost", 5683), mock(ManageDownlink.class));

		final Client registeredClient = clientRegistry.get("device1");
		assertNotNull(registeredClient);
		final ClientResponse response = ReadRequest.newRequest(registeredClient, 1).send(server.getRequestHandler());

		assertEquals(ResponseCode.CONTENT, response.getCode());
		assertEquals("THIS SHOULD HAVE TLV STUFF", new String(response.getContent()));

		client.stop();
	}

}
