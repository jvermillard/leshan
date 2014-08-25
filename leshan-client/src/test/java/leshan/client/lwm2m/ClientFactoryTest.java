package leshan.client.lwm2m;

import static org.junit.Assert.fail;

import java.net.InetSocketAddress;

import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.client.lwm2m.bootstrap.BootstrapUplink;
import leshan.client.lwm2m.factory.ClientFactory;
import leshan.client.lwm2m.register.RegisterDownlink;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.response.Callback;
import leshan.client.lwm2m.response.OperationResponse;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClientFactoryTest {
	@Mock
	private BootstrapDownlink fakeBootstrapListener;
	@Mock
	private RegisterDownlink fakeRegisterListener;

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
