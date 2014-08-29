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
import leshan.server.lwm2m.tlv.TlvEncoder;

public class DeleteTest extends LwM2mClientServerIntegrationTest {

	@Override
	protected LwM2mClient createClient() {
		final ReadWriteListenerWithBrokenWrite brokenResourceListener = new ReadWriteListenerWithBrokenWrite();

		final ClientObject objectOne = new ClientObject(GOOD_OBJECT_ID,
				new SingleResourceDefinition(FIRST_RESOURCE_ID, Executable.NOT_EXECUTABLE, firstResourceListener, firstResourceListener),
				new SingleResourceDefinition(SECOND_RESOURCE_ID, Executable.NOT_EXECUTABLE, secondResourceListener, secondResourceListener),
				new SingleResourceDefinition(EXECUTABLE_RESOURCE_ID, executableAlwaysSuccessful, Writable.NOT_WRITABLE, Readable.NOT_READABLE));
		final ClientObject objectTwo = new ClientObject(BROKEN_OBJECT_ID,
				new SingleResourceDefinition(BROKEN_RESOURCE_ID, Executable.NOT_EXECUTABLE, brokenResourceListener, brokenResourceListener));
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
