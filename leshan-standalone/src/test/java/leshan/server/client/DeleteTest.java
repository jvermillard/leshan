package leshan.server.client;

import leshan.server.lwm2m.request.ClientResponse;
import leshan.server.lwm2m.request.ResponseCode;

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

		final ClientResponse responseDelete = sendDelete(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID);
		assertEmptyResponse(responseDelete, ResponseCode.NOT_FOUND);
	}

	private void createAndThenAssertDeleted() {
		sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse responseDelete = sendDelete(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID);
		assertEmptyResponse(responseDelete, ResponseCode.DELETED);
	}

}
