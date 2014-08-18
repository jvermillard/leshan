package leshan.client.lwm2m;

import java.net.InetSocketAddress;

import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.client.lwm2m.bootstrap.BootstrapUplink;
import leshan.client.lwm2m.factory.ClientFactory;
import leshan.client.lwm2m.response.OperationResponse;

import org.junit.Before;
import org.junit.Test;

public class ClientFactoryTest {
	private BootstrapDownlink fakeDevice;
	
	@Before
	public void setup() {
		fakeDevice = new BootstrapDownlink(){

			@Override
			public OperationResponse write() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public OperationResponse delete() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}
	
	@Test
	public void testGoodRegistrationOfDeviceSync() {
		final ClientFactory clientFactory = new ClientFactory();
		final BootstrapUplink uplink = clientFactory.startBootstrap(InetSocketAddress.createUnresolved("localhost", 1234), fakeDevice);
		OperationResponse response = uplink.bootstrap("endpoint-client-name"); // OR
		
		assert true/*Behavior on Californium via PowerMock*/;
		assert true/*Returned value to FakeDevice*/;
	}
	
	@Test
	public void testGoodRegistrationOfDeviceAsync() {
		final ClientFactory clientFactory = new ClientFactory();
		final BootstrapUplink uplink = clientFactory.startBootstrap(InetSocketAddress.createUnresolved("localhost", 1234), fakeDevice);
		OperationResponse response = uplink.bootstrap("endpoint-client-name"); // OR
		uplink.bootstrap("endpoint-client-name", new Callback<OperationResponse>() {
			
			@Override
			public void onSuccess(OperationResponse t) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(Throwable t) {
				// TODO Auto-generated method stub
				
			}
		});
		
		assert true/*Behavior on Californium via PowerMock*/;
		assert true/*Returned value to FakeDevice*/;
	}

}
