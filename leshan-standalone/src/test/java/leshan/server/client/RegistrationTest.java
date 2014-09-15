package leshan.server.client;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.resource.LwM2mObjectDefinition;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.client.lwm2m.util.ResponseCallback;

import org.junit.Test;

public class RegistrationTest extends LwM2mClientServerIntegrationTest {

	@Test
	public void registeredDeviceExists() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		final OperationResponse register = registerUplink.register(ENDPOINT, clientParameters, TIMEOUT_MS);

		assertTrue(register.isSuccess());
		assertNotNull(getClient());
	}

	@Test(expected=IllegalArgumentException.class)
	public void failToCreateClientWithNull(){
		client = new LwM2mClient((LwM2mObjectDefinition[])null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void failToCreateClientWithSameObjectTwice(){
		final LwM2mObjectDefinition objectOne = new LwM2mObjectDefinition(1, false, false);
		client = new LwM2mClient(objectOne, objectOne);
	}

	@Test
	public void registeredDeviceExistsAsync() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		final ResponseCallback callback = new ResponseCallback();
		registerUplink.register(ENDPOINT, clientParameters, callback);

		await().untilTrue(callback.isCalled());

		assertTrue(callback.isSuccess());
		assertNotNull(getClient());
	}


}
