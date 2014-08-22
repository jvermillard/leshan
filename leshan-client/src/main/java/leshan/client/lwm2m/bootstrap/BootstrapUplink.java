package leshan.client.lwm2m.bootstrap;

import java.io.IOException;
import java.util.Collections;

import leshan.client.lwm2m.BootstrapEndpoint;
import leshan.client.lwm2m.Callback;
import leshan.client.lwm2m.OperationResponseCode;
import leshan.client.lwm2m.register.Uplink;
import leshan.client.lwm2m.response.OperationResponse;
import ch.ethz.inf.vs.californium.coap.MessageObserver;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;

public class BootstrapUplink extends Uplink {
	private static final String ENDPOINT = "ep";
	
	public BootstrapUplink(final CoAPEndpoint endpoint) {
		super(endpoint);
	}
	
	public OperationResponse bootstrap(final String endpointName, final long timeout) {
		final ch.ethz.inf.vs.californium.coap.Request request = ch.ethz.inf.vs.californium.coap.Request.newPost();
		final BootstrapEndpoint bootstrapEndpoint = new BootstrapEndpoint(Collections.singletonMap(ENDPOINT, endpointName));
		request.setURI(bootstrapEndpoint.toString());
		checkStarted(endpoint);
		
		return sendSyncRequest(timeout, request);
	}

	public void bootstrap(final String endpointName, final Callback callback) {
		final ch.ethz.inf.vs.californium.coap.Request request = ch.ethz.inf.vs.californium.coap.Request.newPost();
		final BootstrapEndpoint bootstrapEndpoint = new BootstrapEndpoint(Collections.singletonMap(ENDPOINT, endpointName));
		request.setURI(bootstrapEndpoint.toString());
		
		sendAsyncRequest(callback, request);
	}
	
}
