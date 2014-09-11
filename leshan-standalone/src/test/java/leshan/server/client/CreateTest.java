package leshan.server.client;

import static leshan.server.lwm2m.message.ResponseCode.CREATED;
import static org.junit.Assert.assertEquals;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvType;

import org.junit.Test;

public class CreateTest extends LwM2mClientServerIntegrationTest {

	@Test
	public void canCreateInstanceOfObject() {
		register();

		final ClientResponse response = sendCreate(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);
		assertEmptyResponse(response, ResponseCode.CREATED);
		assertEquals(GOOD_OBJECT_ID + "/0", response.getLocation());
	}

	@Test
	public void canCreateSpecificInstanceOfObject() {
		register();

		final ClientResponse response = sendCreate(createGoodResourcesTlv("one", "two"), GOOD_OBJECT_ID, 14);
		assertEmptyResponse(response, ResponseCode.CREATED);
		assertEquals(GOOD_OBJECT_ID + "/14", response.getLocation());
	}

	@Test
	public void canCreateMultipleInstanceOfObject() {
		register();

		final ClientResponse response = sendCreate(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);
		assertEmptyResponse(response, ResponseCode.CREATED);
		assertEquals(GOOD_OBJECT_ID + "/0", response.getLocation());

		final ClientResponse responseTwo = sendCreate(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);
		assertEmptyResponse(responseTwo, ResponseCode.CREATED);
		assertEquals(GOOD_OBJECT_ID + "/1", responseTwo.getLocation());
	}

	@Test
	public void cannotCreateInstanceOfObject() {
		register();

		final ClientResponse response = sendCreate(createGoodResourcesTlv("hello", "goodbye"), BAD_OBJECT_ID);
		assertEmptyResponse(response, ResponseCode.NOT_FOUND);
	}

	@Test
	public void cannotCreateInstanceWithoutAllRequiredResources() {
		register();
		final Tlv[] values = new Tlv[1];
		values[0] = new Tlv(TlvType.RESOURCE_VALUE, null, "hello".getBytes(), FIRST_RESOURCE_ID);

		final ClientResponse response = sendCreate(values, GOOD_OBJECT_ID);
		assertEmptyResponse(response, ResponseCode.BAD_REQUEST);

		assertEmptyResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.NOT_FOUND);
	}

	@Test
	public void cannotCreateInstanceWithExtraneousResources() {
		register();
		final Tlv[] values = new Tlv[3];
		values[0] = new Tlv(TlvType.RESOURCE_VALUE, null, "hello".getBytes(), FIRST_RESOURCE_ID);
		values[1] = new Tlv(TlvType.RESOURCE_VALUE, null, "goodbye".getBytes(), SECOND_RESOURCE_ID);
		values[2] = new Tlv(TlvType.RESOURCE_VALUE, null, "lolz".getBytes(), INVALID_RESOURCE_ID);

		final ClientResponse response = sendCreate(values, GOOD_OBJECT_ID);
		assertEmptyResponse(response, ResponseCode.METHOD_NOT_ALLOWED);

		assertEmptyResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.NOT_FOUND);
	}

	@Test
	public void cannotCreateInstanceWithNonWritableResource() {
		register();
		final Tlv[] values = new Tlv[3];
		values[0] = new Tlv(TlvType.RESOURCE_VALUE, null, "hello".getBytes(), FIRST_RESOURCE_ID);
		values[1] = new Tlv(TlvType.RESOURCE_VALUE, null, "goodbye".getBytes(), SECOND_RESOURCE_ID);
		values[2] = new Tlv(TlvType.RESOURCE_VALUE, null, "lolz".getBytes(), EXECUTABLE_RESOURCE_ID);

		final ClientResponse response = sendCreate(values, GOOD_OBJECT_ID);
		assertEmptyResponse(response, ResponseCode.METHOD_NOT_ALLOWED);

		assertEmptyResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.NOT_FOUND);
	}

	@Test
	public void canCreateObjectInstanceWithEmptyPayload() {
		register();
		assertEmptyResponse(sendCreate(new Tlv[0], MULTIPLE_OBJECT_ID), CREATED);
	}

}
