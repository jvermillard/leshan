package leshan.server.client;

import org.junit.Test;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.operation.Executable;
import leshan.client.lwm2m.operation.Readable;
import leshan.client.lwm2m.operation.Writable;
import leshan.client.lwm2m.resource.ClientObject;
import leshan.client.lwm2m.resource.SingleResourceDefinition;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ResponseCode;

public class DeleteTest extends LwM2mClientServerIntegrationTest {

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
	public void deleteCreatedObjectInstance(){
		register();

		createAndThenAssertDeleted();
	}

	@Test
	public void deleteAndCantReadObjectInstance(){
		register();

		createAndThenAssertDeleted();

		assertEmptyResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.NOT_FOUND);
	}

	@Test
	public void cantDeleteObject(){
		register();

		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse responseDelete = sendDelete(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID, null);
		assertEmptyResponse(responseDelete, ResponseCode.METHOD_NOT_ALLOWED);
	}

	private void createAndThenAssertDeleted() {
		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse responseDelete = sendDelete(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID);
		assertResponse(responseDelete, ResponseCode.DELETED, new byte[0]);
	}

}
