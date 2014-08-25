package leshan.server.integration.register.normal.register;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.util.UUID;

import leshan.client.lwm2m.Uplink;
import leshan.client.lwm2m.factory.ClientFactory;
import leshan.client.lwm2m.register.RegisterDownlink;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.response.Callback;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.server.lwm2m.message.ResponseCode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.ethz.inf.vs.californium.coap.Request;

@RunWith(MockitoJUnitRunner.class)
public class DeregisterTest extends AbstractRegisteringTest {

	private static final String REGISTRATION_ENDPOINT = "/rd/";
	@Mock
	private RegisterDownlink downlink;
	
	@Test
	public void testCannotDeregisterUnregisteredSync() throws UnknownHostException {
		final ClientFactory clientFactory = new ClientFactory();

		final RegisterUplink registerUplink = clientFactory.startRegistration(clientPort, serverAddress, downlink);
		
		validateNoRegisteredClientOnServer();

		final String validNonexistentLocationPathOptions = REGISTRATION_ENDPOINT + UUID.randomUUID().toString();
		
		final OperationResponse deregisterResponseOne = registerUplink.deregister(validNonexistentLocationPathOptions, TIMEOUT_MS);
		
		assertFalse(deregisterResponseOne.isSuccess());
		assertEquals(deregisterResponseOne.getResponseCode(), ResponseCode.NOT_FOUND);

		final OperationResponse registerResponse = registerUplink.register(clientEndpoint, clientParameters, objectsAndInstances, TIMEOUT_MS);
		final String locationPathOptions = new String(registerResponse.getPayload());
		
		validateRegisteredClientOnServer();
		
		final OperationResponse deregisterResponseTwo = registerUplink.deregister(locationPathOptions, TIMEOUT_MS);
		
		validateResponsesToClient(registerResponse, locationPathOptions, deregisterResponseTwo);

	}
	
	@Test
	public void testCannotDeregisterUnregisteredAsync() throws UnknownHostException {
		final ClientFactory clientFactory = new ClientFactory();

		final RegisterUplink registerUplink = clientFactory.startRegistration(clientPort, serverAddress, downlink);
		
		validateNoRegisteredClientOnServer();

		final String validNonexistentLocationPathOptions = REGISTRATION_ENDPOINT + UUID.randomUUID().toString();
		
		registerUplink.deregister(validNonexistentLocationPathOptions, callback);
		
		await().untilTrue(callback.isCalled());
		
		final OperationResponse deregisterResponseOne = callback.getResponse();
		
		assertFalse(deregisterResponseOne.isSuccess());
		assertEquals(deregisterResponseOne.getResponseCode(), ResponseCode.NOT_FOUND);

		callback.reset();
		registerUplink.register(clientEndpoint, clientParameters, objectsAndInstances, callback);
		
		await().untilTrue(callback.isCalled());
		
		final OperationResponse registerResponse = callback.getResponse();
		
		final String locationPathOptions = new String(registerResponse.getPayload());
		
		validateRegisteredClientOnServer();
		
		callback.reset();
		registerUplink.deregister(locationPathOptions, callback);
		
		await().untilTrue(callback.isCalled());
		
		final OperationResponse deregisterResponseTwo = callback.getResponse();
		
		validateResponsesToClient(registerResponse, locationPathOptions, deregisterResponseTwo);

	}

}
