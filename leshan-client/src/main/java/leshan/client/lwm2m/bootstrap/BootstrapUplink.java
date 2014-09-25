package leshan.client.lwm2m.bootstrap;

import java.net.InetSocketAddress;
import java.util.Collections;

import leshan.client.lwm2m.Uplink;
import leshan.client.lwm2m.response.Callback;
import leshan.client.lwm2m.response.OperationResponse;

import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.CoAPEndpoint;

public class BootstrapUplink extends Uplink {
	private static final String ENDPOINT = "ep";

	public BootstrapUplink(final InetSocketAddress destination, final CoAPEndpoint origin, final BootstrapDownlink downlink) {
		super(destination, origin);
		if(downlink == null){
			throw new IllegalArgumentException("BootstrapDownlink must not be null");
		}
	}

	public OperationResponse bootstrap(final String endpointName, final long timeout) {
		final Request request = Request.newPost();
		final BootstrapEndpoint bootstrapEndpoint = new BootstrapEndpoint(Collections.singletonMap(ENDPOINT, endpointName));
		request.setURI(bootstrapEndpoint.toString());
		checkStarted(origin);

		return sendSyncRequest(timeout, request);
	}

	public void bootstrap(final String endpointName, final Callback callback) {
		final Request request = Request.newPost();
		final BootstrapEndpoint bootstrapEndpoint = new BootstrapEndpoint(Collections.singletonMap(ENDPOINT, endpointName));
		request.setURI(bootstrapEndpoint.toString());

		sendAsyncRequest(callback, request);
	}

}
