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
import ch.ethz.inf.vs.californium.network.Exchange;

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
				request.removeMessageObserver(this);
				callback.onFailure(OperationResponse.failure(ResponseCode.GATEWAY_TIMEOUT, MESSAGE_GATEWAY_TIMEOUT));
			}

			@Override
			public void onRetransmission() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onResponse(final Response response) {
				request.removeMessageObserver(this);
				if(ResponseCode.isSuccess(response.getCode())){
					callback.onSuccess(OperationResponse.of(response));
				}
				else{
					callback.onFailure(OperationResponse.failure(response.getCode(), "Request Failed on Server " + response.getOptions()));
				}
			}

			@Override
			public void onReject() {
				request.removeMessageObserver(this);
				callback.onFailure(OperationResponse.failure(ResponseCode.BAD_GATEWAY, MESSAGE_BAD_GATEWAY));
			}

			@Override
			public void onCancel() {
				request.removeMessageObserver(this);
				callback.onFailure(OperationResponse.failure(ResponseCode.BAD_GATEWAY, MESSAGE_BAD_GATEWAY));
			}

			@Override
			public void onAcknowledgement() {
				request.removeMessageObserver(this);
			}
		});

		checkStarted(origin);
		origin.sendRequest(request);
	}

	protected void sendAsyncResponse(final Exchange exchange, final Response response, final Callback callback) {
		response.addMessageObserver(new MessageObserver() {

			@Override
			public void onTimeout() {
				response.removeMessageObserver(this);
			}

			@Override
			public void onRetransmission() {
				// TODO: Stuff
			}

			@Override
			public void onResponse(final Response response) {
				response.removeMessageObserver(this);
			}

			@Override
			public void onReject() {
				response.removeMessageObserver(this);
			}

			@Override
			public void onCancel() {
				response.removeMessageObserver(this);
			}

			@Override
			public void onAcknowledgement() {
				response.removeMessageObserver(this);
			}

		});

		checkStarted(origin);
		exchange.sendResponse(response);
	}

	protected OperationResponse sendSyncRequest(final long timeout, final ch.ethz.inf.vs.californium.coap.Request request) {
		checkStarted(origin);
		origin.sendRequest(request);

		try {
			final Response response = request.waitForResponse(timeout);

			if(response == null){
				return OperationResponse.failure(ResponseCode.GATEWAY_TIMEOUT, "Timed Out Waiting For Response.");
			}
			else if(ResponseCode.isSuccess(response.getCode())){
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

	public void stop(){
		origin.stop();
	}
}
