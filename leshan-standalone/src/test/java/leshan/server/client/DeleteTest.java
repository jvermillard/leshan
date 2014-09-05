package leshan.server.client;

import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ResponseCode;

import org.junit.Test;

public class DeleteTest extends LwM2mClientServerIntegrationTest {

	@Test
	public void deleteCreatedObjectInstance(){
		register();

		createAndThenAssertDeleted();
	}

	@Test
	public void deleteAndCantReadObjectInstance(){
		register();

		createAndThenAssertDeleted();

		assertEmptyResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.NOT_FOUND);
	}

	@Test
	public void cantDeleteUnknownObjectInstance(){
		register();

		final ClientResponse responseDelete = sendDelete(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID);
		assertEmptyResponse(responseDelete, ResponseCode.NOT_FOUND);
	}

	@Test
	public void cantDeleteObject(){
		register();

		sendCreate(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse responseDelete = sendDelete(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID, null);
		assertEmptyResponse(responseDelete, ResponseCode.METHOD_NOT_ALLOWED);
	}

	private void createAndThenAssertDeleted() {
		sendCreate(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse responseDelete = sendDelete(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID);
		assertResponse(responseDelete, ResponseCode.DELETED, new byte[0]);
	}

}
