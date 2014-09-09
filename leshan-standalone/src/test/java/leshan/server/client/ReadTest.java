package leshan.server.client;

import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.tlv.TlvEncoder;

import org.junit.Test;

public class ReadTest extends LwM2mClientServerIntegrationTest {

	@Test
	public void canReadObject() {
		register();
		assertResponse(sendRead(GOOD_OBJECT_ID), ResponseCode.CONTENT, new byte[0]);
	}

	@Test
	public void canReadObjectWithCreatedInstance() {
		register();
		sendCreate(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		assertResponse(sendRead(GOOD_OBJECT_ID), ResponseCode.CONTENT,
				TlvEncoder.encode(createGoodObjectInstaceTlv("hello", "goodbye")).array());
	}

	@Test
	public void canReadObjectInstance() {
		register();
		sendCreate(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.CONTENT,
				TlvEncoder.encode(createGoodResourcesTlv("hello", "goodbye")).array());
	}

	@Test
	public void canReadResource() {
		register();
		sendCreate(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID),
				ResponseCode.CONTENT, "hello".getBytes());
		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
				ResponseCode.CONTENT, "goodbye".getBytes());
	}

	@Test
	public void cannotReadNonReadableResource() {
		register();
		sendCreate(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		assertEmptyResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, EXECUTABLE_RESOURCE_ID),
				ResponseCode.METHOD_NOT_ALLOWED);
	}

	@Test
	public void cannotReadNonExistentResource() {
		register();
		sendCreate(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		assertEmptyResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INVALID_RESOURCE_ID),
				ResponseCode.NOT_FOUND);
	}

}
