package leshan.client.lwm2m.resource;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

public class WriteResponseTest {

	@Test
	public void canCreateSuccessfulResponse() {
		final WriteResponse response = WriteResponse.success();
		assertEquals(ResponseCode.CHANGED, response.getCode());
	}

	@Test
	public void canCreateFailureResponse() {
		final WriteResponse response = WriteResponse.failure();
		assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
	}

}
