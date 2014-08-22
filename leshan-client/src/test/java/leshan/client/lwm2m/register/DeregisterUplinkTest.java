package leshan.client.lwm2m.register;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import leshan.client.lwm2m.response.OperationResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import ch.ethz.inf.vs.californium.coap.CoAP.Code;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;

@RunWith(MockitoJUnitRunner.class)
public class DeregisterUplinkTest {
	private static final String ENDPOINT_LOCATION = UUID.randomUUID().toString();

	@Mock
	private CoAPEndpoint endpoint;
	
	private String expectedRequestLocation;
	private String actualRequestLocation;
	
	@Before
	public void setUp(){
		expectedRequestLocation = "coap://localhost/rd/" + ENDPOINT_LOCATION;
	}

	@Test
	public void testGoodDeregister() {
		final RegisterUplink uplink = new RegisterUplink(endpoint);
		
		doAnswer(new Answer<Void>() {

			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Request request = (Request) invocation.getArguments()[0];
				actualRequestLocation = request.getURI();
				
				return null;
			}
		}).when(endpoint).sendRequest(any(Request.class));
		
		final OperationResponse response = uplink.deregister(ENDPOINT_LOCATION);
		
		
		verify(endpoint).stop();
		verify(endpoint).sendRequest(any(Request.class));
		
		assertTrue(response.isSuccess());
		assertEquals(ResponseCode.DELETED, response.getResponseCode());
		assertEquals(expectedRequestLocation, actualRequestLocation);
	}
	
	@Test
	public void testNullDeregister() {
		final RegisterUplink uplink = new RegisterUplink(endpoint);
		
		final OperationResponse response = uplink.deregister(null);
		
		verify(endpoint, never()).stop();
		verify(endpoint, never()).sendRequest(any(Request.class));
		
		assertFalse(response.isSuccess());
		assertEquals(ResponseCode.NOT_FOUND, response.getResponseCode());
	}

}
