package leshan.server.client;

import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.tlv.TlvEncoder;

import org.junit.Test;

public class ReadTest extends LwM2mClientServerIntegrationTest {

	@Test
	public void canReadObject() {
		register();
		assertResponse(sendGet(GOOD_OBJECT_ID), ResponseCode.CONTENT, new byte[0]);
	}

	@Test
	public void canReadObjectWithCreatedInstance() {
		register();
		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		assertResponse(sendGet(GOOD_OBJECT_ID), ResponseCode.CONTENT, TlvEncoder.encode(createObjectInstaceTlv("hello", "goodbye")).array());
	}

	@Test
	public void canReadObjectInstace() {
		register();
		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		assertResponse(sendGet(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.CONTENT, TlvEncoder.encode(createResourcesTlv("hello", "goodbye")).array());
	}

	@Test
	public void canReadResource() {
		register();
		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		assertResponse(sendGet(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID),
				ResponseCode.CONTENT, "hello".getBytes());
		assertResponse(sendGet(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
				ResponseCode.CONTENT, "goodbye".getBytes());
	}

}
