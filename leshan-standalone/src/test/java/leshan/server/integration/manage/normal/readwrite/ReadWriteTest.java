package leshan.server.integration.manage.normal.readwrite;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Response;

import com.google.gson.Gson;

import leshan.client.lwm2m.response.OperationResponse;
import leshan.server.clienttest.TestUtils;
import leshan.server.integration.register.normal.register.AbstractRegisteringTest;

@RunWith(MockitoJUnitRunner.class)
public class ReadWriteTest extends AbstractRegisteringTest {

	@Test
	public void testSuccessfulRead() {
		final OperationResponse registerResponse = registerUplink.register(clientEndpoint, clientParameters, TIMEOUT_MS);

		final Gson gson = new Gson();
		
		final String serverKnownClientsJson = TestUtils.getAPI("api/clients");
		List<Map<String, Object>> serverKnownClients = new ArrayList<>();
		serverKnownClients = gson.fromJson(serverKnownClientsJson, serverKnownClients.getClass());
		assertEquals(1, serverKnownClients.size());
	
		final Map<String, Object> clientParameters = serverKnownClients.get(0);
		assertEquals(clientEndpoint, clientParameters.get("endpoint"));
		
		final Response readResponse = new Response(ResponseCode.CONTENT);
		readResponse.setPayload("5");
		when(downlink.read(any(Integer.class), any(Integer.class), any(Integer.class))).
			thenReturn(OperationResponse.of(readResponse));
		
		final boolean operationResult = TestUtils.readOperation(clientEndpoint, "1", null, "101");
		
		assertTrue(operationResult);
	}

}
