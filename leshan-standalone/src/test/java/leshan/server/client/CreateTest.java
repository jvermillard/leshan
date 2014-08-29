package leshan.server.client;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.operation.Executable;
import leshan.client.lwm2m.operation.Readable;
import leshan.client.lwm2m.operation.Writable;
import leshan.client.lwm2m.resource.ClientObject;
import leshan.client.lwm2m.resource.SingleResourceDefinition;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ResponseCode;

import org.junit.Test;

public class CreateTest extends LwM2mClientServerIntegrationTest {

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
	public void canCreateInstanceOfObject() {
		register();

		final ClientResponse response = sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);
		assertResponse(response, ResponseCode.CREATED, ("/" + GOOD_OBJECT_ID + "/0").getBytes());
	}

	@Test
	public void canCreateSpecificInstanceOfObject() {
		register();

		final ClientResponse response = sendCreate(createResourcesTlv("one", "two"), GOOD_OBJECT_ID, 14);
		assertResponse(response, ResponseCode.CREATED, ("/" + GOOD_OBJECT_ID + "/14").getBytes());
	}

	@Test
	public void canCreateMultipleInstanceOfObject() {
		register();

		final ClientResponse response = sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);
		assertResponse(response, ResponseCode.CREATED, ("/" + GOOD_OBJECT_ID + "/0").getBytes());

		final ClientResponse responseTwo = sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);
		assertResponse(responseTwo, ResponseCode.CREATED, ("/" + GOOD_OBJECT_ID + "/1").getBytes());
	}

	@Test
	public void canNotCreateInstanceOfObject() {
		register();

		final ClientResponse response = sendCreate(createResourcesTlv("hello", "goodbye"), BAD_OBJECT_ID);
		assertEmptyResponse(response, ResponseCode.NOT_FOUND);
	}

}
