package leshan.server.client;

import static org.junit.Assert.assertArrayEquals;
import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.resource.ClientObject;
import leshan.client.lwm2m.resource.ExecuteListener;
import leshan.client.lwm2m.resource.ReadListener;
import leshan.client.lwm2m.resource.SingleResourceDefinition;
import leshan.client.lwm2m.resource.WriteListener;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.message.WriteRequest;

import org.junit.Ignore;
import org.junit.Test;

public class WriteTest extends LwM2mClientServerIntegrationTest {

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
	public void canWriteReplaceToResource() {
		register();

		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse response = WriteRequest.newReplaceRequest(getClient(), GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID,
				"world", ContentFormat.TEXT).send(server.getRequestHandler());

		assertResponse(response, ResponseCode.CHANGED, new byte[0]);
		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
				ResponseCode.CONTENT, "world".getBytes());
		assertArrayEquals(secondResourceListener.read(), "world".getBytes());
	}

	@Test
	public void badWriteReplaceToResource() {
		register();

		sendCreate(createBrokenResourcesTlv("i'm broken!"), BROKEN_OBJECT_ID);

		final ClientResponse response = WriteRequest.newReplaceRequest(getClient(), BROKEN_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, BROKEN_RESOURCE_ID,
				"fix me!", ContentFormat.TEXT).send(server.getRequestHandler());

		assertEmptyResponse(response, ResponseCode.BAD_REQUEST);
		assertResponse(sendRead(BROKEN_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, BROKEN_RESOURCE_ID),
				ResponseCode.CONTENT, "i'm broken!".getBytes());
	}

	// TODO: This test tests something that is untestable by the LWM2M spec and should
	// probably be deleted. Ignored until this is confirmed
	@Ignore
	@Test
	public void canWritePartialUpdateToResource() {
		register();

		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse response = WriteRequest.newUpdateRequest(getClient(), GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID,
				"world", ContentFormat.TEXT).send(server.getRequestHandler());

		assertResponse(response, ResponseCode.CHANGED, new byte[0]);
		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
				ResponseCode.CONTENT, "world".getBytes());
	}

}
