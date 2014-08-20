package leshan.client.lwm2m.bootstrap;

import java.io.IOException;
import java.util.Collections;

import leshan.client.lwm2m.BootstrapEndpoint;
import leshan.client.lwm2m.Callback;
import leshan.client.lwm2m.response.OperationResponse;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.MessageObserver;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;

public class BootstrapUplink {
	private static final String ENDPOINT = "ep";
	private final CoAPEndpoint endpoint;
	
	public BootstrapUplink(CoAPEndpoint endpoint) {
		this.endpoint = endpoint;
	}
	
	public OperationResponse bootstrap(final String endpointName, final long timeout) {
		ch.ethz.inf.vs.californium.coap.Request request = ch.ethz.inf.vs.californium.coap.Request.newPost();
		BootstrapEndpoint bootstrapEndpoint = new BootstrapEndpoint(Collections.singletonMap(ENDPOINT, endpointName));
		request.setPayload(bootstrapEndpoint.toString());
		checkStarted(endpoint);
		endpoint.sendRequest(request);
		
		try {
			Response response = request.waitForResponse(timeout);
			return OperationResponse.of(response);
		} catch (InterruptedException e) {
			// TODO: Am I an internal server error?
			return OperationResponse.failure(ResponseCode.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	public void bootstrap(final String endpointName, final Callback callback) {
		ch.ethz.inf.vs.californium.coap.Request request = ch.ethz.inf.vs.californium.coap.Request.newPost();
		BootstrapEndpoint bootstrapEndpoint = new BootstrapEndpoint(Collections.singletonMap(ENDPOINT, endpointName));
		request.setPayload(bootstrapEndpoint.toString());
		request.addMessageObserver(new MessageObserver() {
			
			@Override
			public void onTimeout() {
				// TODO Auto-generated method stub
				callback.onFailure(OperationResponse.failure(ResponseCode.GATEWAY_TIMEOUT));
			}
			
			@Override
			public void onRetransmission() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onResponse(Response response) {
				callback.onSuccess(OperationResponse.of(response));
			}
			
			@Override
			public void onReject() {
				// TODO Auto-generated method stub
				callback.onFailure(OperationResponse.failure(ResponseCode.BAD_GATEWAY));
			}
			
			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
				callback.onFailure(OperationResponse.failure(ResponseCode.BAD_GATEWAY));
			}
			
			@Override
			public void onAcknowledgement() {
				// TODO Auto-generated method stub
			}
		});
		
		checkStarted(endpoint);
		endpoint.se
		endpoint.sendRequest(request);
	}
	
	private static void checkStarted(CoAPEndpoint endpoint) {
		if(!endpoint.isStarted()) {
			try {
				endpoint.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
