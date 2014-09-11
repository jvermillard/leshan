package leshan.server.client;

import static leshan.server.lwm2m.message.ResponseCode.BAD_REQUEST;
import static leshan.server.lwm2m.message.ResponseCode.CHANGED;
import static leshan.server.lwm2m.message.ResponseCode.CONTENT;
import static leshan.server.lwm2m.message.ResponseCode.METHOD_NOT_ALLOWED;
import static leshan.server.lwm2m.tlv.TlvType.RESOURCE_INSTANCE;
import static leshan.server.lwm2m.tlv.TlvType.RESOURCE_VALUE;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.WriteRequest;
import leshan.server.lwm2m.tlv.Tlv;

import org.junit.Ignore;
import org.junit.Test;

public class WriteTest extends LwM2mClientServerIntegrationTest {

	private static final String HELLO = "hello";
	private static final String GOODBYE = "goodbye";

	@Test
	public void canWriteReplaceToResource() {
		register();

		sendCreate(createGoodResourcesTlv(HELLO, GOODBYE), GOOD_OBJECT_ID);

		final ClientResponse response = WriteRequest.newReplaceRequest(getClient(), GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID,
				"world", ContentFormat.TEXT).send(server.getRequestHandler());

		assertResponse(response, CHANGED, new byte[0]);
		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
				CONTENT, "world".getBytes());
		assertEquals("world", secondResource.getValue());
	}

	@Test
	public void badWriteReplaceToResource() {
		register();

		sendCreate(createBrokenResourcesTlv("i'm broken!"), BROKEN_OBJECT_ID);

		final ClientResponse response = WriteRequest.newReplaceRequest(getClient(), BROKEN_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, BROKEN_RESOURCE_ID,
				"fix me!", ContentFormat.TEXT).send(server.getRequestHandler());

		assertEmptyResponse(response, BAD_REQUEST);
		assertResponse(sendRead(BROKEN_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, BROKEN_RESOURCE_ID),
				CONTENT, "i'm broken!".getBytes());
	}

	@Test
	public void cannotWriteToNonWritableResource() {
		register();

		sendCreate(createGoodResourcesTlv(HELLO, GOODBYE), GOOD_OBJECT_ID);

		final ClientResponse response = WriteRequest.newReplaceRequest(getClient(), GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, EXECUTABLE_RESOURCE_ID,
				"world", ContentFormat.TEXT).send(server.getRequestHandler());

		assertEmptyResponse(response, METHOD_NOT_ALLOWED);
	}

	@Test
	public void canWriteToWritableMultipleResource() {
		register();
		sendCreate(new Tlv[0], MULTIPLE_OBJECT_ID);

		final Tlv[] tlvs = new Tlv[] {
			new Tlv(RESOURCE_INSTANCE, null, HELLO.getBytes(), 0),
			new Tlv(RESOURCE_INSTANCE, null, GOODBYE.getBytes(), 1)
		};

		final Map<Integer, byte[]> map = new HashMap<Integer, byte[]>();
		map.put(0, HELLO.getBytes());
		map.put(1, GOODBYE.getBytes());

		multipleResource.setValue(map);

		assertEmptyResponse(sendReplace(tlvs, MULTIPLE_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, MULTIPLE_RESOURCE_ID), CHANGED);
	}

	// TODO: This test tests something that is untestable by the LWM2M spec and should
	// probably be deleted. Ignored until this is confirmed
	@Ignore
	@Test
	public void canWritePartialUpdateToResource() {
		register();

		sendCreate(createGoodResourcesTlv(HELLO, GOODBYE), GOOD_OBJECT_ID);

		final ClientResponse response = WriteRequest.newUpdateRequest(getClient(), GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID,
				"world", ContentFormat.TEXT).send(server.getRequestHandler());

		assertResponse(response, CHANGED, new byte[0]);
		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
				CONTENT, "world".getBytes());
	}

	protected Tlv[] createBrokenResourcesTlv(final String value) {
		final Tlv[] values = new Tlv[1];
		values[0] = new Tlv(RESOURCE_VALUE, null, value.getBytes(), BROKEN_RESOURCE_ID);
		return values;
	}

}
