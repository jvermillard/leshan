package leshan.client.lwm2m;

import static org.junit.Assert.*;
import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.client.lwm2m.bootstrap.BootstrapUplink;
import leshan.client.lwm2m.factory.ClientFactory;
import leshan.client.lwm2m.response.OperationResponse;

import org.junit.Test;

public class ClientFactoryTest {

	@Test
	public void test() {
		final BootstrapDownlink fakeDevice = new BootstrapDownlink(){

			public OperationResponse write() {
				// TODO Auto-generated method stub
				return null;
			}

			public OperationResponse delete() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		
		final ClientFactory clientFactory = new ClientFactory();
		final BootstrapUplink uplink = clientFactory.startBootstrap(fakeDevice);
		uplink.bootstrap();
	}

}
