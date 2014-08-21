package leshan.client.lwm2m;

import static org.junit.Assert.fail;

import java.net.InetSocketAddress;

import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.client.lwm2m.bootstrap.BootstrapUplink;
import leshan.client.lwm2m.factory.ClientFactory;
import leshan.client.lwm2m.response.OperationResponse;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ClientFactoryTest {
	private BootstrapDownlink fakeDevice;

	@Before
	public void setup() {
		fakeDevice = new BootstrapDownlink(){

			@Override
			public OperationResponse write(final int objectId, final int objectInstanceId, final int resourceId) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public OperationResponse delete(final int objectId, final int objectInstanceId) {
				// TODO Auto-generated method stub
				return null;
			}

		};
	}

	@Ignore
	@Test
	public void testGoodRegistrationOfDeviceSync() {
		final ClientFactory clientFactory = new ClientFactory();
		final BootstrapUplink uplink = clientFactory.startBootstrap(InetSocketAddress.createUnresolved("localhost", 1234), fakeDevice);
		final OperationResponse response = uplink.bootstrap("endpoint-client-name", 5L);
		assert response.isSuccess();
		assert true/*Behavior on Californium via PowerMock*/;
		assert true/*Returned value to FakeDevice*/;
	}

	@Test
	public void testGoodRegistrationOfDeviceAsync() {
		final ClientFactory clientFactory = new ClientFactory();
		final BootstrapUplink uplink = clientFactory.startBootstrap(InetSocketAddress.createUnresolved("localhost", 1234), fakeDevice);
		uplink.bootstrap("endpoint-client-name", new Callback() {

			@Override
			public void onSuccess(final OperationResponse response) {
				fail("Failure!");
			}

			@Override
			public void onFailure(final OperationResponse response) {

			}
		});

		assert true/*Behavior on Californium via PowerMock*/;
		assert true/*Returned value to FakeDevice*/;
	}

}
