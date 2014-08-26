package leshan.client.lwm2m.factory;

import java.net.InetSocketAddress;

import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.client.lwm2m.bootstrap.BootstrapUplink;
import leshan.client.lwm2m.factory.ClientFactory;
import leshan.client.lwm2m.manage.ManageDownlink;
import leshan.client.lwm2m.register.RegisterUplink;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClientFactoryTest {
	@Mock
	private BootstrapDownlink fakeBootstrapListener;
	@Mock
	private ManageDownlink fakeRegisterListener;

	@Before
	public void setup() {
	}

	@Test
	public void testLegalBootstrapUplinkCreate() {
		final ClientFactory clientFactory = new ClientFactory();
		final BootstrapUplink uplink = clientFactory.startBootstrap(4321, InetSocketAddress.createUnresolved("localhost", 1234), fakeBootstrapListener);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalNullBootstrapUplinkCreate() {
		final ClientFactory clientFactory = new ClientFactory();
		final BootstrapUplink uplink = clientFactory.startBootstrap(4321, InetSocketAddress.createUnresolved("localhost", 1234), null);
	}
	
	@Test
	public void testLegalRegisterUplinkCreate() {
		final ClientFactory clientFactory = new ClientFactory();
		final RegisterUplink uplink = clientFactory.startRegistration(4321, InetSocketAddress.createUnresolved("localhost", 1234), fakeRegisterListener);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalNullRegisterUplinkCreate() {
		final ClientFactory clientFactory = new ClientFactory();
		final RegisterUplink uplink = clientFactory.startRegistration(4321, InetSocketAddress.createUnresolved("localhost", 1234), null);
	}

}
