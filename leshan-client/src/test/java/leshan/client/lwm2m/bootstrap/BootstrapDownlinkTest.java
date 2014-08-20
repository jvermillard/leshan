package leshan.client.lwm2m.bootstrap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import leshan.client.lwm2m.BootstrapMessageDeliverer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import ch.ethz.inf.vs.californium.coap.CoAP.Code;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.network.Exchange;

import com.google.common.base.Joiner;

@RunWith(MockitoJUnitRunner.class)
public class BootstrapDownlinkTest {

	private static final int OBJECT_ID = 3;
	private static final int OBJECT_INSTANCE_ID = 1;
	private static final int RESOURCE_ID = 2;

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
		BootstrapDownlink downlink = mock(BootstrapDownlink.class);
		BootstrapMessageDeliverer deliverer = new BootstrapMessageDeliverer(downlink);
	
		Exchange exchange = mock(Exchange.class);
		Request request = mock(Request.class);
		when(request.getCode()).thenReturn(Code.PUT);
		when(request.getURI()).thenReturn(constructUri(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID));
		
		when(exchange.getRequest()).thenReturn(request);
		deliverer.deliverRequest(exchange);
		
		verify(downlink).write(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID);
	}
	
	
	@Test
	public void testDeleteGoodPayload() {
	}
	
	private static String constructUri(final int objectId, final int objectInstanceId, final int resourceId) {
		return "/" + Joiner.on("/").skipNulls().join(objectId, objectInstanceId, resourceId);
	}
}
