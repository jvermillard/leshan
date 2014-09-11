package leshan.server.client;

import static leshan.server.lwm2m.message.ResponseCode.CONTENT;
import static leshan.server.lwm2m.message.ResponseCode.METHOD_NOT_ALLOWED;
import static leshan.server.lwm2m.message.ResponseCode.NOT_FOUND;
import static leshan.server.lwm2m.tlv.TlvType.MULTIPLE_RESOURCE;
import static leshan.server.lwm2m.tlv.TlvType.RESOURCE_INSTANCE;

import java.util.HashMap;
import java.util.Map;

import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvEncoder;

import org.junit.Test;

public class ReadTest extends LwM2mClientServerIntegrationTest {

	private static final String HELLO = "hello";
	private static final String GOODBYE = "goodbye";

	@Test
	public void canReadObject() {
		register();
		assertResponse(sendRead(GOOD_OBJECT_ID), CONTENT, new byte[0]);
	}

	@Test
	public void canReadObjectWithCreatedInstance() {
		register();
		sendCreate(createGoodResourcesTlv(HELLO, GOODBYE), GOOD_OBJECT_ID);

		assertResponse(sendRead(GOOD_OBJECT_ID), CONTENT, TlvEncoder.encode(createGoodObjectInstaceTlv(HELLO, GOODBYE)).array());
	}

	@Test
	public void canReadObjectInstance() {
		register();
		sendCreate(createGoodResourcesTlv(HELLO, GOODBYE), GOOD_OBJECT_ID);

		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), CONTENT, TlvEncoder.encode(createGoodResourcesTlv(HELLO, GOODBYE)).array());
	}

	@Test
	public void canReadResource() {
		register();
		sendCreate(createGoodResourcesTlv(HELLO, GOODBYE), GOOD_OBJECT_ID);

		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID), CONTENT, HELLO.getBytes());
		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID), CONTENT, GOODBYE.getBytes());
	}

	@Test
	public void cannotReadNonReadableResource() {
		register();
		sendCreate(createGoodResourcesTlv(HELLO, GOODBYE), GOOD_OBJECT_ID);

		assertEmptyResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, EXECUTABLE_RESOURCE_ID), METHOD_NOT_ALLOWED);
	}

	@Test
	public void cannotReadNonExistentResource() {
		register();
		sendCreate(createGoodResourcesTlv(HELLO, GOODBYE), GOOD_OBJECT_ID);

		assertEmptyResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INVALID_RESOURCE_ID), NOT_FOUND);
	}

	@Test
	public void canReadMultipleResource() {
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

		final byte[] payload = TlvEncoder.encode(tlvs).array();
		assertResponse(sendRead(MULTIPLE_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, MULTIPLE_RESOURCE_ID), CONTENT, payload);
	}


	// TODO: This doesn't seem like it should be passing. Figure out why it is.
	// Expected response TLV type to be RESOURCE_VALUE
	@Test
	public void canReadObjectInstanceWithMultipleResource() {
		register();
		sendCreate(new Tlv[0], MULTIPLE_OBJECT_ID);

		final Tlv[] tlvs = new Tlv[] {
			new Tlv(RESOURCE_INSTANCE, null, HELLO.getBytes(), 0),
			new Tlv(RESOURCE_INSTANCE, null, GOODBYE.getBytes(), 1)
		};

		final Tlv tlv = new Tlv(MULTIPLE_RESOURCE, tlvs, null, MULTIPLE_RESOURCE_ID);

		final Map<Integer, byte[]> map = new HashMap<Integer, byte[]>();
		map.put(0, HELLO.getBytes());
		map.put(1, GOODBYE.getBytes());

		multipleResource.setValue(map);

		final byte[] payload = TlvEncoder.encode(new Tlv[]{tlv}).array();
		assertResponse(sendRead(MULTIPLE_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), CONTENT, payload);
	}

}
