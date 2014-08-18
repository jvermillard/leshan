package leshan.client.lwm2m.bootstrap;

import java.util.Collections;

import leshan.client.lwm2m.BootstrapEndpoint;
import leshan.client.lwm2m.response.OperationResponse;
import ch.ethz.inf.vs.californium.coap.MessageObserver;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;

public class BootstrapUplink {
	private static final String ENDPOINT = "ep";
	private final CoAPEndpoint endpoint;
	
	public BootstrapUplink(CoAPEndpoint endpoint) {
		this.endpoint = endpoint;
	}
	
	public OperationResponse bootstrap(String endpointName) {
		ch.ethz.inf.vs.californium.coap.Request request = ch.ethz.inf.vs.californium.coap.Request.newPost();
		BootstrapEndpoint bootstrapEndpoint = new BootstrapEndpoint(Collections.singletonMap(ENDPOINT, endpointName));
		request.setPayload(bootstrapEndpoint.toString());
		endpoint.sendRequest(request);
		
		try {
			Response response = request.waitForResponse();
			return OperationResponse.of(response);
		} catch (InterruptedException e) {
			return OperationResponse.failure();
		}
	}
	
	public void bootstrap(final String endpointName, final Callback<OperationResponse> callback) {
		ch.ethz.inf.vs.californium.coap.Request request = ch.ethz.inf.vs.californium.coap.Request.newPost();
		BootstrapEndpoint bootstrapEndpoint = new BootstrapEndpoint(Collections.singletonMap(ENDPOINT, endpointName));
		request.setPayload(bootstrapEndpoint.toString());
		request.addMessageObserver(new MessageObserver() {
			
			@Override
			public void onTimeout() {
				// TODO Auto-generated method stub
				callback.onFailure(null);
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
				callback.onFailure(null);
			}
			
			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
				callback.onFailure(null);
			}
			
			@Override
			public void onAcknowledgement() {
				// TODO Auto-generated method stub
			}
		});
		
		endpoint.sendRequest(request);
	}
}
