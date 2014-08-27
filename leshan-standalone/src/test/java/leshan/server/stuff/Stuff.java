package leshan.server.stuff;

import java.net.InetSocketAddress;

import leshan.server.lwm2m.LwM2mServer;
import leshan.server.lwm2m.bootstrap.BootstrapStoreImpl;
import leshan.server.lwm2m.client.ClientRegistryImpl;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.observation.ObservationRegistryImpl;
import leshan.server.lwm2m.security.SecurityRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.CoapClient;
import ch.ethz.inf.vs.californium.CoapResponse;

public class Stuff {

	private final String VALID_REQUEST_PAYLOAD = "</lwm2m>;rt=\"oma.lwm2m\", </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";

	private static final Logger LOG = LoggerFactory.getLogger(Stuff.class);
	private LwM2mServer server;

	@Before
	public void setup() {
		final InetSocketAddress address = new InetSocketAddress(5683);
		final InetSocketAddress addressSecure = new InetSocketAddress(5684);
		final ClientRegistryImpl clientRegistry = new ClientRegistryImpl();
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
	public void registrationTest() {
		final CoapClient client = new CoapClient("coap://localhost:5683/rd?ep=device1");
		final CoapResponse post = client.post(VALID_REQUEST_PAYLOAD, 1);
		LOG.info("Result of POST: " + post);
	}

}
