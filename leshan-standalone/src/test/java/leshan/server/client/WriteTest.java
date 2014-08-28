package leshan.server.client;

import static org.junit.Assert.assertArrayEquals;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.message.WriteRequest;

import org.junit.Ignore;
import org.junit.Test;

public class WriteTest extends LwM2mClientServerIntegrationTest {

	@Test
	public void canWriteReplaceToResource() {
		register();

		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse response = WriteRequest.newReplaceRequest(getClient(), GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID,
				"world", ContentFormat.TEXT).send(server.getRequestHandler());

		assertResponse(response, ResponseCode.CHANGED, new byte[0]);
		assertResponse(sendGet(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
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
		assertResponse(sendGet(BROKEN_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, BROKEN_RESOURCE_ID),
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
		assertResponse(sendGet(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
				ResponseCode.CONTENT, "world".getBytes());
	}

}
