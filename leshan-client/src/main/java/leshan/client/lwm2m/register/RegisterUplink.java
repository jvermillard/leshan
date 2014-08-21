package leshan.client.lwm2m.register;

import java.io.IOException;
import java.util.Collections;

import ch.ethz.inf.vs.californium.coap.MessageObserver;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;
import leshan.client.lwm2m.Callback;
import leshan.client.lwm2m.response.OperationResponse;

public class RegisterUplink extends Uplink{
	private static final String ENDPOINT = "ep";
	private final CoAPEndpoint endpoint;

	public RegisterUplink(final CoAPEndpoint endpoint) {
		this.endpoint = endpoint;
	}
	
	public OperationResponse register(final String endpointName, final int timeout) {
		final ch.ethz.inf.vs.californium.coap.Request request = ch.ethz.inf.vs.californium.coap.Request.newPost();
		final RegisterEndpoint bootstrapEndpoint = new RegisterEndpoint(Collections.singletonMap(ENDPOINT, endpointName));
		request.setURI(bootstrapEndpoint.toString());
		checkStarted(endpoint);
		endpoint.sendRequest(request);
		
		try {
			final Response response = request.waitForResponse(timeout);
			return OperationResponse.of(response);
		} catch (final InterruptedException e) {
			// TODO: Am I an internal server error?
			return OperationResponse.failure(ResponseCode.INTERNAL_SERVER_ERROR);
		}
	}
	
	public void register(final String endpointName, final Callback callback) {
		final ch.ethz.inf.vs.californium.coap.Request request = ch.ethz.inf.vs.californium.coap.Request.newPost();
		final RegisterEndpoint registerEndpoint = new RegisterEndpoint(Collections.singletonMap(ENDPOINT, endpointName));
		request.setURI(registerEndpoint.toString());
		
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
			public void onResponse(final Response response) {
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
		endpoint.sendRequest(request);
	}
	
	public void delete(final String location, final Callback callback) {
		// TODO Auto-generated method stub
		
	}
	
	public OperationResponse update(final String location, final Callback callback) {
		return null;
	}
	public OperationResponse deregister() {
		return null;
	}
	//...
	public OperationResponse notify(final String todo) {
		return null;
	}

}
