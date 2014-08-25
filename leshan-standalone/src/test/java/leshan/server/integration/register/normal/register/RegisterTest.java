package leshan.server.integration.register.normal.register;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
import leshan.client.lwm2m.Uplink;
import leshan.client.lwm2m.factory.ClientFactory;
import leshan.client.lwm2m.register.RegisterDownlink;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.response.Callback;
import leshan.client.lwm2m.response.OperationResponse;
import org.junit.Test;

public class RegisterTest extends AbstractRegisteringTest {

	private RegisterDownlink downlink;
	
	@Test
	public void testRegisterAndDeregister() throws UnknownHostException {
		final ClientFactory clientFactory = new ClientFactory();

		final RegisterUplink registerUplink = clientFactory.startRegistration(clientPort, serverAddress, downlink);

		final OperationResponse registerResponse = registerUplink.register(clientEndpoint, clientParameters, objectsAndInstances, TIMEOUT_MS);

		final String locationPathOptions = new String(registerResponse.getPayload());

		validateRegisteredClientOnServer();

		final OperationResponse deregisterResponse = registerUplink.deregister(clientEndpoint);

		validateNoRegisteredClientOnServer();

		validateResponsesToClient(registerResponse, locationPathOptions, deregisterResponse);

	}

}
