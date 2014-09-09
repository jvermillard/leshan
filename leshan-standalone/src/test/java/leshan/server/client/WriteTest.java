package leshan.server.client;

import static org.junit.Assert.assertEquals;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.message.WriteRequest;
import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvType;

import org.junit.Ignore;
import org.junit.Test;

public class WriteTest extends LwM2mClientServerIntegrationTest {

	@Test
	public void canWriteReplaceToResource() {
		register();

		sendCreate(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse response = WriteRequest.newReplaceRequest(getClient(), GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID,
				"world", ContentFormat.TEXT).send(server.getRequestHandler());

		assertResponse(response, ResponseCode.CHANGED, new byte[0]);
		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
				ResponseCode.CONTENT, "world".getBytes());
		assertEquals("world", secondResource.getValue());
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

	@Test
	public void cannotWriteToNonWritableResource() {
		register();

		sendCreate(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse response = WriteRequest.newReplaceRequest(getClient(), GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, EXECUTABLE_RESOURCE_ID,
				"world", ContentFormat.TEXT).send(server.getRequestHandler());

		assertEmptyResponse(response, ResponseCode.METHOD_NOT_ALLOWED);
	}

	// TODO: This test tests something that is untestable by the LWM2M spec and should
	// probably be deleted. Ignored until this is confirmed
	@Ignore
	@Test
	public void canWritePartialUpdateToResource() {
		register();

		sendCreate(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse response = WriteRequest.newUpdateRequest(getClient(), GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID,
				"world", ContentFormat.TEXT).send(server.getRequestHandler());

		assertResponse(response, ResponseCode.CHANGED, new byte[0]);
		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
				ResponseCode.CONTENT, "world".getBytes());
	}

	protected Tlv[] createBrokenResourcesTlv(final String value) {
		final Tlv[] values = new Tlv[1];
		values[0] = new Tlv(TlvType.RESOURCE_VALUE, null, value.getBytes(), BROKEN_RESOURCE_ID);
		return values;
	}

}
