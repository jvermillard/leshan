package leshan.client.lwm2m.report;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.UUID;

import leshan.client.lwm2m.bootstrap.BootstrapMessageDelivererTest;
import leshan.client.lwm2m.util.ResponseCallback;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import ch.ethz.inf.vs.californium.coap.CoAP.Code;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.OptionSet;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;
import ch.ethz.inf.vs.californium.network.Exchange;

@RunWith(MockitoJUnitRunner.class)
public class ReportDownlinkTest {

	private static final int OBJECT_ID = 3;
	private static final int OBJECT_INSTANCE_ID = 1;
	private static final int RESOURCE_ID = 2;
	private static final String SERVER_HOST = "leshan.com";
	private static final int SERVER_PORT = 1234;


	private InetSocketAddress serverAddress;
	private Response actualResponse;
	private ReportMessageDeliverer messageDeliverer;

	@Mock
	private ReportDownlink downlink;

	@Mock
	private Exchange exchange;

	@Mock
	private Request request;

	@Mock
	private CoAPEndpoint endpoint;
	private ReportUplink uplink;
	private byte[] actualToken;
	private byte[] expectedToken;


	@Before
	public void setup() {
		this.serverAddress = InetSocketAddress.createUnresolved(SERVER_HOST, SERVER_PORT);
		this.messageDeliverer = new ReportMessageDeliverer(downlink);
		this.expectedToken = Arrays.copyOfRange(UUID.randomUUID().toString().getBytes(), 0, 8);
	}

	@Test
	public void testReceiveObservation() {
		final String resourceSpec = BootstrapMessageDelivererTest.constructUri(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID);
		initializeRequestReturn(resourceSpec, expectedToken);

		initializeResponse();
		initializeServerResponses(Code.GET);


		this.messageDeliverer.deliverRequest(exchange);

		assertEquals(ResponseCode.CONTENT, actualResponse.getCode());
		verify(downlink).observe(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID, expectedToken);

		actualToken = actualResponse.getToken();

		assertArrayEquals(expectedToken, actualToken);
	}

	@Test
	public void testReceiveObservationAndNotify() {

		final String resourceSpec = BootstrapMessageDelivererTest.constructUri(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID);
		initializeResponse();
		initializeRequestReturn(resourceSpec, expectedToken);
		initializeServerResponses(Code.GET);

		this.messageDeliverer.deliverRequest(exchange);

		assertEquals(actualResponse.getCode(), ResponseCode.CONTENT);
		verify(downlink).observe(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID, expectedToken);

		final byte[] localToken = actualResponse.getToken();

		actualResponse = null;
		actualToken = null;

		final byte[] actualNewValue = "123456".getBytes();
		final ResponseCallback actualCallback = new ResponseCallback();
		uplink.notify(localToken, actualNewValue, actualCallback);

		assertEquals(actualResponse.getCode(), ResponseCode.CHANGED);
		assertEquals(actualResponse.getPayload(), actualNewValue);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testReceiveObservationBadToken() {
		final byte[] actualNewValue = "123456".getBytes();
		final ResponseCallback actualCallback = new ResponseCallback();

		uplink = new ReportUplink(serverAddress, endpoint);
		uplink.notify("llama".getBytes(), actualNewValue, actualCallback);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testReceiveObservationNullToken() {
		final byte[] actualNewValue = "123456".getBytes();
		final ResponseCallback actualCallback = new ResponseCallback();

		uplink = new ReportUplink(serverAddress, endpoint);
		uplink.notify(null, actualNewValue, actualCallback);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testReceiveObservationNullNewValue() {
		final ResponseCallback actualCallback = new ResponseCallback();

		uplink = new ReportUplink(serverAddress, endpoint);
		uplink.notify("123".getBytes(), null, actualCallback);
	}


	@Test(expected = IllegalArgumentException.class)
	public void testReceiveObservationNullCallback() {
		final byte[] actualNewValue = "123456".getBytes();

		uplink = new ReportUplink(serverAddress, endpoint);
		uplink.notify("123".getBytes(), actualNewValue, null);
	}

	@Test
	public void testReceiveBadUriObservation() {
		initializeResponse();
		initializeRequestReturn("/NaN", expectedToken);
		initializeServerResponses(Code.GET);

		this.messageDeliverer.deliverRequest(exchange);

		assertEquals(actualResponse.getCode(), ResponseCode.NOT_FOUND);
		verify(downlink, never()).observe(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID, expectedToken);
	}

	@Test
	public void testReceiveIllegalCode() {
		initializeResponse();

		final String resourceSpec = BootstrapMessageDelivererTest.constructUri(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID);
		initializeRequestReturn(resourceSpec, actualToken);

		initializeServerResponses(Code.PUT);
		this.messageDeliverer.deliverRequest(exchange);

		assertNull(actualResponse);

		verify(downlink, never()).observe(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID, actualToken);
	}

	@Test
	public void testCancelObservation() {
		testReceiveObservationAndNotify();

		verify(downlink).cancelObservation(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID);
	}

	private void initializeServerResponses(final Code code) {
		final OptionSet options = mock(OptionSet.class);
		when(options.hasObserve()).thenReturn(true);
		when(request.getOptions()).thenReturn(options);
		when(request.getCode()).thenReturn(code);
		when(request.getToken()).thenReturn(expectedToken);
	}

	private void initializeResponse() {
		doAnswer(new Answer<Void>(){

			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				actualResponse = (Response) invocation.getArguments()[0];
				return null;
			}
		}).when(exchange).sendResponse(any(Response.class));

		uplink = new ReportUplink(serverAddress, endpoint);
	}

	private void initializeCanceledResponse() {
		doAnswer(new Answer<Void>(){

			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				actualResponse = (Response) invocation.getArguments()[0];
				actualResponse.setCanceled(true);
				return null;
			}
		}).when(exchange).sendResponse(any(Response.class));

		uplink = new ReportUplink(serverAddress, endpoint);
	}

	private void initializeRequestReturn(final String uri, final byte[] token) {
		when(request.getURI()).thenReturn(uri);
		when(request.getToken()).thenReturn(token);
		when(exchange.getRequest()).thenReturn(request, request);
	}

}
