package leshan.client.lwm2m.register;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import ch.ethz.inf.vs.californium.WebLink;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.coap.MessageObserver;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;
import leshan.client.lwm2m.Callback;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.client.lwm2m.util.LinkFormatUtils;

public class RegisterUplink extends Uplink{
	private static final String ENDPOINT = "ep";
	private final CoAPEndpoint endpoint;

	public RegisterUplink(final CoAPEndpoint endpoint) {
		this.endpoint = endpoint;
	}

	public OperationResponse register(final String endpointName, final Map<String, String> parameters, final Set<WebLink> objectsAndInstances, final int timeout) {
		if(parameters == null || !areParametersValid(parameters) || objectsAndInstances == null){
			return OperationResponse.failure(ResponseCode.BAD_REQUEST);
		}

		final String payload = LinkFormatUtils.payloadize(objectsAndInstances);
		if(payload == null){
			return OperationResponse.failure(ResponseCode.BAD_REQUEST);
		}

		final ch.ethz.inf.vs.californium.coap.Request request = ch.ethz.inf.vs.californium.coap.Request.newPost();
		final RegisterEndpoint bootstrapEndpoint = new RegisterEndpoint(Collections.singletonMap(ENDPOINT, endpointName));

		request.setURI(bootstrapEndpoint.toString() + "&" + leshan.client.lwm2m.Request.toQueryStringMap(parameters));
		request.setPayload(payload);
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

	public void register(final String endpointName, final Map<String, String> parameters, final Set<WebLink> objectsAndInstances, final Callback callback) {
		if(parameters == null || !areParametersValid(parameters) || objectsAndInstances == null){
			callback.onFailure(OperationResponse.failure(ResponseCode.BAD_REQUEST));
			return;
		}

		final String payload = LinkFormatUtils.payloadize(objectsAndInstances);
		if(payload == null){
			callback.onFailure(OperationResponse.failure(ResponseCode.BAD_REQUEST));
			return;
		}

		final ch.ethz.inf.vs.californium.coap.Request request = ch.ethz.inf.vs.californium.coap.Request.newPost();
		final RegisterEndpoint registerEndpoint = new RegisterEndpoint(Collections.singletonMap(ENDPOINT, endpointName));
		request.setURI(registerEndpoint.toString());
		request.setPayload(payload);

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
	public OperationResponse deregister(final String endpointName) {
		if(endpointName == null){
			return OperationResponse.failure(ResponseCode.NOT_FOUND);
		}
		
		final ch.ethz.inf.vs.californium.coap.Request request = ch.ethz.inf.vs.californium.coap.Request.newDelete();
		final DeregisterEndpoint deregisterEndpoint = new DeregisterEndpoint(endpointName);
		request.setURI(deregisterEndpoint.toString());
		
		endpoint.sendRequest(request);
		endpoint.stop();
		
		return OperationResponse.of(new Response(ResponseCode.DELETED));
	}
	//...
	public OperationResponse notify(final String todo) {
		return null;
	}

	private boolean areParametersValid(final Map<String, String> parameters) {
		for(final Map.Entry<String, String> p : parameters.entrySet()){
			switch(p.getKey()){
			case "lt" :
				break;
			case "lwm2m" :
				break;
			case "sms" :
				return false;
			case "b" :
				if(!isBindingValid(p.getValue())){
					return false;
				}
				break;
			default:
				return false;
			}
		}

		return true;
	}

	private boolean isBindingValid(final String value) {
		if(value.equals("U")){
			return true;
		}

		return false;
	}
}
