package leshan.server.client;

import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ResponseCode;

import org.junit.Test;

public class CreateTest extends LwM2mClientServerIntegrationTest {

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
