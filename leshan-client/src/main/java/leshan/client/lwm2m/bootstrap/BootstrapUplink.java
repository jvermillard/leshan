package leshan.client.lwm2m.bootstrap;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;

import leshan.client.lwm2m.Uplink;
import leshan.client.lwm2m.response.Callback;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.client.lwm2m.response.OperationResponseCode;
import ch.ethz.inf.vs.californium.coap.MessageObserver;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;

public class BootstrapUplink extends Uplink {
	private static final String ENDPOINT = "ep";
	
	public BootstrapUplink(final InetSocketAddress destination, final CoAPEndpoint origin) {
		super(destination, origin);
	}
	
	public OperationResponse bootstrap(final String endpointName, final long timeout) {
		final ch.ethz.inf.vs.californium.coap.Request request = ch.ethz.inf.vs.californium.coap.Request.newPost();
		final BootstrapEndpoint bootstrapEndpoint = new BootstrapEndpoint(Collections.singletonMap(ENDPOINT, endpointName));
		request.setURI(bootstrapEndpoint.toString());
		checkStarted(origin);
		
		return sendSyncRequest(timeout, request);
	}

	public void bootstrap(final String endpointName, final Callback callback) {
		final ch.ethz.inf.vs.californium.coap.Request request = ch.ethz.inf.vs.californium.coap.Request.newPost();
		final BootstrapEndpoint bootstrapEndpoint = new BootstrapEndpoint(Collections.singletonMap(ENDPOINT, endpointName));
		request.setURI(bootstrapEndpoint.toString());
		
		sendAsyncRequest(callback, request);
	}
	
}
