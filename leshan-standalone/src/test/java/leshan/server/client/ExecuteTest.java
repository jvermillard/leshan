package leshan.server.client;

import leshan.server.lwm2m.request.ClientResponse;
import leshan.server.lwm2m.request.ContentFormat;
import leshan.server.lwm2m.request.ExecuteRequest;
import leshan.server.lwm2m.request.ResponseCode;

import org.junit.Test;

public class ExecuteTest extends LwM2mClientServerIntegrationTest {

	@Test
	public void canNotExecuteWriteOnlyResource() {
		register();

		sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse response = server.send(
				new ExecuteRequest(
						getClient(),
						GOOD_OBJECT_ID,
						GOOD_OBJECT_INSTANCE_ID,
						SECOND_RESOURCE_ID,
						"world".getBytes(),
						ContentFormat.TEXT));

		assertEmptyResponse(response, ResponseCode.METHOD_NOT_ALLOWED);
	}

	@Test
	public void canExecuteResource() {
		register();

		sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse response = server.send(
				new ExecuteRequest(
						getClient(),
						GOOD_OBJECT_ID,
						GOOD_OBJECT_INSTANCE_ID,
						EXECUTABLE_RESOURCE_ID,
                        "world".getBytes(),
						ContentFormat.TEXT));

		assertEmptyResponse(response, ResponseCode.CHANGED);
//		executableResource).execute(any(LwM2mExchange.class));
	}

}
