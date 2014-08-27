package leshan.server.stuff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;

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

import ch.ethz.inf.vs.californium.CoapClient;
import ch.ethz.inf.vs.californium.CoapResponse;
import ch.ethz.inf.vs.californium.network.Endpoint;
import ch.ethz.inf.vs.californium.server.Server;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class Stuff {

	private static final String LWM2M_SERVER_ADDRESS = "coap://localhost:5683/rd?ep=device1";

	private final String VALID_REQUEST_PAYLOAD = "</lwm2m>;rt=\"oma.lwm2m\", </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";

	private LwM2mServer server;
	private ClientRegistryImpl clientRegistry;

	@Before
	public void setup() {
		final InetSocketAddress address = new InetSocketAddress(5683);
		final InetSocketAddress addressSecure = new InetSocketAddress(5684);
		clientRegistry = new ClientRegistryImpl();
		final ObservationRegistry observationRegistry = new ObservationRegistryImpl();
		final SecurityRegistry securityRegistry = new SecurityRegistry();
		final BootstrapStoreImpl bsStore = new BootstrapStoreImpl();
		server = new LwM2mServer(address, addressSecure, clientRegistry, securityRegistry, observationRegistry, bsStore);
		server.start();
	}

	@After
	public void teardown() {
		server.stop();
	}

	@Test
	public void registrationRespondsWithCreated() {
		final CoapClient client = new CoapClient(LWM2M_SERVER_ADDRESS);
		final CoapResponse post = client.post(VALID_REQUEST_PAYLOAD, 1);
		assertEquals("", post.getResponseText());
		assertEquals(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CREATED, post.getCode());
	}

	@Test
	public void registeredDeviceCanHaveReadSentToIt() {
		final CoapClient client = new CoapClient(LWM2M_SERVER_ADDRESS);

		final Server clientSideServer = new Server(44022);
		final Endpoint endpoint = clientSideServer.getEndpoint(44022);
		client.setEndpoint(endpoint);

		final Resource readResource = new ObjectResource(1);
		clientSideServer.add(readResource);
		clientSideServer.start();

		client.post(VALID_REQUEST_PAYLOAD, 1);

		final Client registeredClient = clientRegistry.get("device1");
		assertNotNull(registeredClient);
		final ClientResponse response = ReadRequest.newRequest(registeredClient, 1).send(server.getRequestHandler());

		assertEquals(ResponseCode.CONTENT, response.getCode());
		assertEquals("THIS SHOULD HAVE TLV STUFF", new String(response.getContent()));

		clientSideServer.stop();
	}

}


class ObjectResource extends ResourceBase {

	private final String value;

	public ObjectResource(final int objectId) {
		super(Integer.toString(objectId));
		value = "THIS SHOULD HAVE TLV STUFF";
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		exchange.respond(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT, value);
	}

}
