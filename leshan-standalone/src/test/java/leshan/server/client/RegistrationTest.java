package leshan.server.client;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.operation.Executable;
import leshan.client.lwm2m.operation.Readable;
import leshan.client.lwm2m.operation.Writable;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.resource.ClientObject;
import leshan.client.lwm2m.resource.SingleResourceDefinition;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.client.lwm2m.util.ResponseCallback;

import org.junit.Test;

public class RegistrationTest extends LwM2mClientServerIntegrationTest {

	@Override
	protected LwM2mClient createClient() {
		final ReadWriteListenerWithBrokenWrite brokenResourceListener = new ReadWriteListenerWithBrokenWrite();

		final ClientObject objectOne = new ClientObject(GOOD_OBJECT_ID,
				new SingleResourceDefinition(FIRST_RESOURCE_ID, firstReadableWritable, firstReadableWritable, Executable.NOT_EXECUTABLE),
				new SingleResourceDefinition(SECOND_RESOURCE_ID, secondReadableWritable, secondReadableWritable, Executable.NOT_EXECUTABLE),
				new SingleResourceDefinition(EXECUTABLE_RESOURCE_ID, Readable.NOT_READABLE, Writable.NOT_WRITABLE, executableAlwaysSuccessful));
		final ClientObject objectTwo = new ClientObject(BROKEN_OBJECT_ID,
				new SingleResourceDefinition(BROKEN_RESOURCE_ID, brokenResourceListener, brokenResourceListener, Executable.NOT_EXECUTABLE));
		return new LwM2mClient(objectOne, objectTwo);
	}

	@Test
	public void registeredDeviceExists() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		final OperationResponse register = registerUplink.register(ENDPOINT, clientParameters, TIMEOUT_MS);

		assertTrue(register.isSuccess());
		assertNotNull(getClient());
	}

	@Test(expected=IllegalArgumentException.class)
	public void failToCreateClientWithNull(){
		client = new LwM2mClient((ClientObject[])null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void failToCreateClientWithSameObjectTwice(){
		final ClientObject objectOne = new ClientObject(1);
		client = new LwM2mClient(objectOne, objectOne);
	}

	@Test
	public void registeredDeviceExistsAsync() {
		final RegisterUplink registerUplink = registerAndGetUplink();
		final ResponseCallback callback = new ResponseCallback();
		registerUplink.register(ENDPOINT, clientParameters, objectsAndInstances, callback);

		await().untilTrue(callback.isCalled());

		assertTrue(callback.isSuccess());
		assertNotNull(getClient());
	}


}
