package leshan.client.lwm2m;

import java.net.InetSocketAddress;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.client.lwm2m.bootstrap.BootstrapUplink;
import leshan.client.lwm2m.manage.ManageDownlink;
import leshan.client.lwm2m.register.RegisterUplink;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.ethz.inf.vs.californium.server.Server;

@RunWith(MockitoJUnitRunner.class)
public class LwM2mClientTest {
	@Mock
	private BootstrapDownlink fakeBootstrapDownlink;
	@Mock
	private ManageDownlink fakeRegisterDownlink;
	
	@Mock
	private Server server;

	@Before
	public void setup() {
	}

	@Test
	public void testLegalBootstrapUplinkCreate() {
		final LwM2mClient client = new LwM2mClient();
		final BootstrapUplink uplink = client.startBootstrap(4321, InetSocketAddress.createUnresolved("localhost", 1234), fakeBootstrapDownlink);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testIllegalNullBootstrapUplinkCreate() {
		final LwM2mClient client = new LwM2mClient();
		final BootstrapUplink uplink = client.startBootstrap(4321, InetSocketAddress.createUnresolved("localhost", 1234), null);
	}

	@Test
	public void testLegalRegisterUplinkCreate() {
		final LwM2mClient client = new LwM2mClient(server);
		final RegisterUplink uplink = client.startRegistration(4321, InetSocketAddress.createUnresolved("localhost", 1234), fakeRegisterDownlink);
		
	}

	@Test(expected=IllegalArgumentException.class)
	public void testIllegalNullRegisterUplinkCreate() {
		final LwM2mClient client = new LwM2mClient();
		final RegisterUplink uplink = client.startRegistration(4321, InetSocketAddress.createUnresolved("localhost", 1234), null);
	}

}
