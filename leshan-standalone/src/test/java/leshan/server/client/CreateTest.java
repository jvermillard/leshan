package leshan.server.client;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.resource.ClientObject;
import leshan.client.lwm2m.resource.ExecuteListener;
import leshan.client.lwm2m.resource.ReadListener;
import leshan.client.lwm2m.resource.SingleResourceDefinition;
import leshan.client.lwm2m.resource.WriteListener;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ResponseCode;

import org.junit.Test;

public class CreateTest extends LwM2mClientServerIntegrationTest {

	@Override
	protected LwM2mClient createClient() {
		final ReadWriteListenerWithBrokenWrite brokenResourceListener = new ReadWriteListenerWithBrokenWrite();

		final ClientObject objectOne = new ClientObject(GOOD_OBJECT_ID,
				new SingleResourceDefinition(FIRST_RESOURCE_ID, ExecuteListener.DUMMY, firstResourceListener, firstResourceListener),
				new SingleResourceDefinition(SECOND_RESOURCE_ID, ExecuteListener.DUMMY, secondResourceListener, secondResourceListener),
				new SingleResourceDefinition(EXECUTABLE_RESOURCE_ID, executeListener, WriteListener.DUMMY, ReadListener.DUMMY));
		final ClientObject objectTwo = new ClientObject(BROKEN_OBJECT_ID,
				new SingleResourceDefinition(BROKEN_RESOURCE_ID, ExecuteListener.DUMMY, brokenResourceListener, brokenResourceListener));
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
