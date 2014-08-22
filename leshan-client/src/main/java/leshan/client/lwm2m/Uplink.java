package leshan.client.lwm2m;

import java.io.IOException;

import leshan.client.lwm2m.response.Callback;
import leshan.client.lwm2m.response.OperationResponse;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.MessageObserver;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;

public abstract class Uplink {

	protected final CoAPEndpoint endpoint;

	public Uplink(final CoAPEndpoint endpoint) {
		this.endpoint = endpoint;
	}

	public final void checkStarted(final CoAPEndpoint endpoint) {
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
				callback.onFailure(OperationResponse.failure(ResponseCode.BAD_GATEWAY));
			}
			
			@Override
			public void onCancel() {
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

	protected OperationResponse sendSyncRequest(final long timeout, final ch.ethz.inf.vs.californium.coap.Request request) {
		endpoint.sendRequest(request);
		
		try {
			final Response response = request.waitForResponse(timeout);
			return OperationResponse.of(response);
		} catch (final InterruptedException e) {
			// TODO: Am I an internal server error?
			return OperationResponse.failure(ResponseCode.INTERNAL_SERVER_ERROR);
		}
	}

}
