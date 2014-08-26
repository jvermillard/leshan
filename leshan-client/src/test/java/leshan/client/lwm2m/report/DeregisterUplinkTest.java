package leshan.client.lwm2m.report;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.InterfaceTypes;
import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.OperationTypes;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.response.MockedCallback;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.client.lwm2m.response.OperationResponseCode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;

@RunWith(MockitoJUnitRunner.class)
public class DeregisterUplinkTest {
	private static final String ENDPOINT_LOCATION = UUID.randomUUID().toString();

	@Mock
	private CoAPEndpoint endpoint;

	private String expectedRequestLocation;
	private String actualRequestLocation;

	private RegisterUplink uplink;

	private MockedCallback callback;

	private byte[] actualResponsePayload;

	@Before
	public void setUp(){
		callback = new MockedCallback();
		expectedRequestLocation = "coap://localhost/rd/" + ENDPOINT_LOCATION;
		uplink = new RegisterUplink(endpoint);

		doAnswer(new Answer<Void>() {

			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Request request = (Request) invocation.getArguments()[0];
				actualRequestLocation = request.getURI();

				final Response response = new Response(ResponseCode.DELETED);
				response.setPayload(OperationResponseCode.generateReasonPhrase(OperationResponseCode.valueOf(response.getCode().value),
						InterfaceTypes.REGISTRATION, OperationTypes.DEREGISTER));

				request.setResponse(response);

				return null;
			}
		}).when(endpoint).sendRequest(any(Request.class));
	}

	@Test
	public void testGoodSyncDeregister() {

		final OperationResponse response = uplink.deregister(ENDPOINT_LOCATION);


		verify(endpoint).stop();
		verify(endpoint).sendRequest(any(Request.class));

		assertTrue(response.isSuccess());
		assertEquals(ResponseCode.DELETED, response.getResponseCode());
		assertEquals(expectedRequestLocation, actualRequestLocation);
	}

	@Test
	public void testGoodAsyncDeregister() {

		uplink.deregister(ENDPOINT_LOCATION, callback);

		await().untilTrue(callback.isCalled());
		actualResponsePayload = callback.getResponsePayload();

		verify(endpoint).stop();
		verify(endpoint).sendRequest(any(Request.class));

		assertTrue(callback.isSuccess());
		assertEquals(ResponseCode.DELETED, callback.getResponseCode());
		assertEquals(expectedRequestLocation, actualRequestLocation);
	}

	@Test
	public void testNullSyncDeregister() {
		final OperationResponse response = uplink.deregister(null);

		verify(endpoint, never()).stop();
		verify(endpoint, never()).sendRequest(any(Request.class));

		assertFalse(response.isSuccess());
		assertEquals(ResponseCode.NOT_FOUND, response.getResponseCode());
	}

}
