package leshan.server.integration.register.normal.register;

import static com.jayway.awaitility.Awaitility.await;
import java.net.UnknownHostException;

import leshan.client.lwm2m.response.OperationResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RegisterTest extends AbstractRegisteringTest {
	
	private static final long DEFAULT_LIFETIME = 86400L;

	@Test
	public void testRegisterAndDeregisterSync() throws UnknownHostException {
		final OperationResponse registerResponse = registerUplink.register(clientEndpoint, clientParameters, TIMEOUT_MS);

		final String locationPath = new String(registerResponse.getLocation());

		validateRegisteredClientOnServer(86400L);

		final OperationResponse deregisterResponse = registerUplink.deregister(locationPath, TIMEOUT_MS);

		validateNoRegisteredClientOnServer();

		validateResponsesToClient(registerResponse, locationPath, deregisterResponse);

	}

	@Test
	public void testRegisterAndDeregisterAsync() throws UnknownHostException {
		registerUplink.register(clientEndpoint, clientParameters, callback);
		
		await().untilTrue(callback.isCalled());

		final String locationPath = new String(callback.getResponse().getLocation());
		final OperationResponse registerResponse = callback.getResponse();

		validateRegisteredClientOnServer(DEFAULT_LIFETIME);

		callback.reset();
		registerUplink.deregister(locationPath, callback);
		
		await().untilTrue(callback.isCalled());
		
		final OperationResponse deregisterResponse = callback.getResponse();

		validateNoRegisteredClientOnServer();

		validateResponsesToClient(registerResponse, locationPath, deregisterResponse);

	}
	
	@Test
	public void testRegisterAndDeregisterWithParametersSync() throws UnknownHostException {
		final Long newLifetime = (long) 100002;
		clientParameters.put("lt", newLifetime.toString());
		
		final OperationResponse registerResponse = registerUplink.register(clientEndpoint, clientParameters, TIMEOUT_MS);

		final String locationPath = new String(registerResponse.getLocation());

		validateRegisteredClientOnServer(newLifetime);

		final OperationResponse deregisterResponse = registerUplink.deregister(locationPath, TIMEOUT_MS);

		validateNoRegisteredClientOnServer();

		validateResponsesToClient(registerResponse, locationPath, deregisterResponse);

	}
}
