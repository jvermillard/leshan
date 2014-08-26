package leshan.client.lwm2m.register;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;
import java.util.UUID;

import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.InterfaceTypes;
import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.OperationTypes;
import leshan.client.lwm2m.manage.ManageDownlink;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.client.lwm2m.response.OperationResponseCode;
import leshan.client.lwm2m.util.ResponseCallback;

import org.junit.After;
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
	private static final int SYNC_TIMEOUT_MS = 2000;
	private static final String SERVER_HOST = "leshan.com";
	private static final int SERVER_PORT = 1234;

	private static final String ENDPOINT_LOCATION = UUID.randomUUID().toString();

	@Mock
	private CoAPEndpoint endpoint;
	@Mock
	private ManageDownlink downlink;
	
	private String actualRequestLocation;

	private RegisterUplink uplink;

	private ResponseCallback callback;

	private byte[] actualResponsePayload;

	private InetSocketAddress serverAddress;
	private int tearDownEndpointStops;
	
	@Before
	public void setUp(){
		callback = new ResponseCallback();
		serverAddress = InetSocketAddress.createUnresolved(SERVER_HOST, SERVER_PORT);
		uplink = new RegisterUplink(serverAddress, endpoint, downlink);
		
		doAnswer(new Answer<Void>() {

			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Request request = (Request) invocation.getArguments()[0];
				actualRequestLocation = request.getOptions().getLocationPathString();
				
				final Response response = new Response(ResponseCode.DELETED);
				response.setPayload(OperationResponseCode.generateReasonPhrase(OperationResponseCode.valueOf(response.getCode().value), 
						InterfaceTypes.REGISTRATION, OperationTypes.DEREGISTER));

				request.setResponse(response);
				
				return null;
			}
		}).when(endpoint).sendRequest(any(Request.class));
	}
	
	@After
	public void tearDown(){
		uplink.stop();
		
		verify(endpoint, times(tearDownEndpointStops)).stop();
	}

	@Test
	public void testGoodSyncDeregister() {
		tearDownEndpointStops = 2;
		
		final OperationResponse response = uplink.deregister(ENDPOINT_LOCATION, SYNC_TIMEOUT_MS);
		
		
		verify(endpoint).stop();
		verify(endpoint).sendRequest(any(Request.class));
		
		assertTrue(response.isSuccess());
		assertEquals(ResponseCode.DELETED, response.getResponseCode());
		assertEquals(ENDPOINT_LOCATION, actualRequestLocation);
	}
	
	@Test
	public void testGoodAsyncDeregister() {
		tearDownEndpointStops = 2;
		
		uplink.deregister(ENDPOINT_LOCATION, callback);
		
		await().untilTrue(callback.isCalled());
		actualResponsePayload = callback.getResponsePayload();
		
		verify(endpoint).stop();
		verify(endpoint).sendRequest(any(Request.class));
		
		assertTrue(callback.isSuccess());
		assertEquals(ResponseCode.DELETED, callback.getResponseCode());
		assertEquals(ENDPOINT_LOCATION, actualRequestLocation);
	}
	
	@Test
	public void testNullSyncDeregister() {
		tearDownEndpointStops = 1;
		
		final OperationResponse response = uplink.deregister(null, SYNC_TIMEOUT_MS);
		
		verify(endpoint, never()).stop();
		verify(endpoint, never()).sendRequest(any(Request.class));
		
		assertFalse(response.isSuccess());
		assertEquals(ResponseCode.NOT_FOUND, response.getResponseCode());
	}

}
