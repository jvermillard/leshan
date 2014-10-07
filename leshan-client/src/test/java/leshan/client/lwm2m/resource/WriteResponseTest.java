package leshan.client.lwm2m.resource;

import static org.junit.Assert.assertEquals;
import leshan.client.lwm2m.response.OperationResponseCode;
import leshan.client.lwm2m.response.WriteResponse;

import org.junit.Test;

public class WriteResponseTest {

	@Test
	public void canCreateSuccessfulResponse() {
		final WriteResponse response = WriteResponse.success();
		assertEquals(OperationResponseCode.CHANGED, response.getCode());
	}

	@Test
	public void canCreateFailureResponse() {
		final WriteResponse response = WriteResponse.failure();
		assertEquals(OperationResponseCode.BAD_REQUEST, response.getCode());
	}

}
