package leshan.server.integration.register.normal.register;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import leshan.client.lwm2m.Uplink;
import leshan.client.lwm2m.factory.ClientFactory;
import leshan.client.lwm2m.register.RegisterDownlink;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.response.Callback;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.server.clienttest.TestUtils;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

import com.google.gson.Gson;

@RunWith(MockitoJUnitRunner.class)
public class UpdateTest extends AbstractRegisteringTest {

	@Mock
	private RegisterDownlink downlink;
	
	
	@Test
	public void testRegisterUpdateAndDeregisterSync() throws UnknownHostException {
		final ClientFactory clientFactory = new ClientFactory();

		final RegisterUplink registerUplink = clientFactory.startRegistration(clientPort, serverAddress, downlink);

		final OperationResponse registerResponse = registerUplink.register(clientEndpoint, clientParameters, objectsAndInstances, TIMEOUT_MS);

		final String locationPath = new String(registerResponse.getLocation());

		final Double newLifetime = 100000.1;
		clientParameters.put("lt", newLifetime.toString());
		
		final OperationResponse updateResponse = registerUplink.update(locationPath, clientParameters, objectsAndInstances, TIMEOUT_MS);
		
		registerUplink.deregister(locationPath, TIMEOUT_MS);
		
		assertTrue(updateResponse.isSuccess());
		assertEquals(ResponseCode.CHANGED, updateResponse.getResponseCode());
		
		validateUpdatedClientOnServer(newLifetime);

	}

	@Test
	public void testRegisterUpdateAndDeregisterAsync() throws UnknownHostException {
		final ClientFactory clientFactory = new ClientFactory();

		final RegisterUplink registerUplink = clientFactory.startRegistration(clientPort, serverAddress, downlink);
		
		registerUplink.register(clientEndpoint, clientParameters, objectsAndInstances, callback);
		
		await().untilTrue(callback.isCalled());

		final String locationPath = new String(callback.getResponse().getLocation());

		final Double newLifetime = 100000.1;
		clientParameters.put("lt", newLifetime.toString());

		callback.reset();
		registerUplink.update(locationPath, clientParameters, objectsAndInstances, callback);
		
		await().untilTrue(callback.isCalled());
		
		assertTrue(callback.isSuccess());
		assertEquals(ResponseCode.CHANGED, callback.getResponseCode());
		
		validateUpdatedClientOnServer(newLifetime);

		callback.reset();
		registerUplink.deregister(locationPath, callback);
		
		await().untilTrue(callback.isCalled());
	}
	
	private void validateUpdatedClientOnServer(final Double lifetime) {
		final Gson gson = new Gson();
		
		final String serverKnownClientsJson = TestUtils.getAPI("api/clients");
		List<Map<String, Object>> serverKnownClients = new ArrayList<>();
		serverKnownClients = gson.fromJson(serverKnownClientsJson, serverKnownClients.getClass());
		assertEquals(1, serverKnownClients.size());
		
		final Map<String, Object> clientParameters = serverKnownClients.get(0);
		assertEquals(lifetime, clientParameters.get("lifetime"));
	}
}
