package leshan.client.lwm2m.register;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import leshan.client.lwm2m.QuietCallback;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import ch.ethz.inf.vs.californium.coap.CoAP.Code;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class RegisterUplinkTest {

	private static final String ENDPOINT_NAME = UUID.randomUUID().toString();
	private String actualRequest;
	private Code actualCode;

	@Test
	public void testWriteGoodPayload() {
		final CoAPEndpoint endpoint = mock(CoAPEndpoint.class);

		doAnswer(new Answer<Void>(){

			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Request request = (Request) invocation.getArguments()[0];
				actualRequest = request.getPayloadString();
				actualCode = request.getCode();

				return null;
			}
		}).when(endpoint).sendRequest(any(Request.class));

		final RegisterUplink uplink = new RegisterUplink(endpoint);

		uplink.register(ENDPOINT_NAME, new QuietCallback());

		final String expectedRequest = "/rd?ep=" + ENDPOINT_NAME;

		assertEquals(expectedRequest, actualRequest);
		assertEquals(Code.POST, actualCode);
	}


	@Test
	public void testUpdateGoodPayload() {
		final CoAPEndpoint endpoint = mock(CoAPEndpoint.class);

		doAnswer(new Answer<Void>(){

			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Request request = (Request) invocation.getArguments()[0];
				actualRequest = request.getPayloadString();
				actualCode = request.getCode();

				return null;
			}
		}).when(endpoint).sendRequest(any(Request.class));

		final RegisterUplink uplink = new RegisterUplink(endpoint);

		final String location = "/rd/ " + ENDPOINT_NAME;

		uplink.update(location, new QuietCallback());

		final String expectedRequest = location;

		assertEquals(expectedRequest, actualRequest);
		assertEquals(Code.PUT, actualCode);
	}

	@Test
	public void testDeregisterGoodPayload() {
		final CoAPEndpoint endpoint = mock(CoAPEndpoint.class);

		doAnswer(new Answer<Void>(){

			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Request request = (Request) invocation.getArguments()[0];
				actualRequest = request.getPayloadString();
				actualCode = request.getCode();

				return null;
			}
		}).when(endpoint).sendRequest(any(Request.class));

		final RegisterUplink uplink = new RegisterUplink(endpoint);

		final String location = "/rd/ " + ENDPOINT_NAME;

		uplink.delete(location, new QuietCallback());

		final String expectedRequest = "/rd?ep=" + ENDPOINT_NAME;

		assertEquals(expectedRequest, actualRequest);
		assertEquals(Code.DELETE, actualCode);
	}
}
