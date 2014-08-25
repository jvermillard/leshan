package leshan.client.lwm2m;

import java.io.IOException;
import java.net.InetSocketAddress;

import leshan.client.lwm2m.response.Callback;
import leshan.client.lwm2m.response.OperationResponse;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.MessageObserver;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;

public abstract class Uplink {

	private static final String MESSAGE_BAD_GATEWAY = "Bad Gateway on Async Callback";
	private static final String MESSAGE_GATEWAY_TIMEOUT = "Gateway Timed Out on Asynch Callback";
	private static final String MESSAGE_INTERRUPTED = "Endpoint Interrupted While Waiting for Sync Response";
	protected final CoAPEndpoint origin;
	private final InetSocketAddress destination;

	public Uplink(final InetSocketAddress destination, final CoAPEndpoint origin) {
		this.destination = destination;
		this.origin = origin;
	}

	protected final void checkStarted(final CoAPEndpoint endpoint) {
		if(!endpoint.isStarted()) {
			try {
				endpoint.start();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected void sendAsyncRequest(final Callback callback, final Request request) {
		request.addMessageObserver(new MessageObserver() {
			
			@Override
			public void onTimeout() {
				callback.onFailure(OperationResponse.failure(ResponseCode.GATEWAY_TIMEOUT, MESSAGE_GATEWAY_TIMEOUT));
			}
			
			@Override
			public void onRetransmission() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onResponse(final Response response) {
				if(ResponseCode.isSuccess(response.getCode())){
					callback.onSuccess(OperationResponse.of(response));
				}
				else{
					callback.onFailure(OperationResponse.failure(response.getCode(), "Request Failed on Server " + response.getOptions()));
				}
			}
			
			@Override
			public void onReject() {
				callback.onFailure(OperationResponse.failure(ResponseCode.BAD_GATEWAY, MESSAGE_BAD_GATEWAY));
			}
			
			@Override
			public void onCancel() {
				callback.onFailure(OperationResponse.failure(ResponseCode.BAD_GATEWAY, MESSAGE_BAD_GATEWAY));
			}
			
			@Override
			public void onAcknowledgement() {
				// TODO Auto-generated method stub
			}
		});
		
		checkStarted(origin);
		origin.sendRequest(request);
	}

	protected OperationResponse sendSyncRequest(final long timeout, final ch.ethz.inf.vs.californium.coap.Request request) {
		checkStarted(origin);
		origin.sendRequest(request);
		
		try {
			final Response response = request.waitForResponse(timeout);
			if(ResponseCode.isSuccess(response.getCode())){
				return OperationResponse.of(response);
			}
			else{
				return OperationResponse.failure(response.getCode(), "Request Failed on Server " + response.getOptions());
			}
		} catch (final InterruptedException e) {
			// TODO: Am I an internal server error?
			return OperationResponse.failure(ResponseCode.INTERNAL_SERVER_ERROR, MESSAGE_INTERRUPTED);
		}
	}
	
	protected InetSocketAddress getDestination() {
		return destination;
	}
}
