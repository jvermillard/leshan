package leshan.client.lwm2m.bootstrap;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.UUID;

import leshan.client.lwm2m.QuietCallback;
import leshan.client.lwm2m.response.OperationResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import ch.ethz.inf.vs.californium.coap.CoAP.Code;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;

@RunWith(MockitoJUnitRunner.class)
public class BootstrapDownlinkTest {

	private static final String ENDPOINT_NAME = UUID.randomUUID().toString();
	private String actualRequest;
	private Code actualCode;

	@Test
	public void testWriteNoInstanceGoodPayload() {
	}
	
	@Test
	public void testWriteRootGoodPayload() {
	}
	
	@Test
	public void testWriteResourceGoodPayload() {
	}
	
	
	@Test
	public void testDeleteGoodPayload() {
	}
}
