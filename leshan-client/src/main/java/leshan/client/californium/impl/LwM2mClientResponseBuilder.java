package leshan.client.californium.impl;

import java.util.logging.Logger;

import leshan.client.request.DeregisterRequest;
import leshan.client.request.LwM2mClientRequestVisitor;
import leshan.client.request.RegisterRequest;
import leshan.client.response.OperationResponse;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;

public class LwM2mClientResponseBuilder implements LwM2mClientRequestVisitor {
	private static final Logger LOG = Logger.getLogger(LwM2mClientResponseBuilder.class.getCanonicalName());

	private final Request coapRequest;
	private final Response coapResponse;
	private final CaliforniumLwM2mClientRequestSender californiumLwM2mClientRequestSender;
	private OperationResponse lwM2mresponse;

	public LwM2mClientResponseBuilder(final Request coapRequest,
			final Response coapResponse, final CaliforniumLwM2mClientRequestSender californiumLwM2mClientRequestSender) {
		this.coapRequest = coapRequest;
		this.coapResponse = coapResponse;
		this.californiumLwM2mClientRequestSender = californiumLwM2mClientRequestSender;
	}

	@Override
	public void visit(final RegisterRequest request) {
		//TODO run this through the eclipse stylesheet
		LOG.info("Got Register Request response.");
		if (coapResponse == null) {
			lwM2mresponse = OperationResponse.failure(ResponseCode.GATEWAY_TIMEOUT, "Timed Out Waiting For Response.");
		} else if (ResponseCode.isSuccess(coapResponse.getCode())) {
			lwM2mresponse = OperationResponse.of(coapResponse);
		} else {
			lwM2mresponse = OperationResponse.failure(coapResponse.getCode(),
					"Request Failed on Server " + coapResponse.getOptions());
		}

	}

	@Override
	public void visit(final DeregisterRequest request) {
		//TODO possibly need to stop the endpoint... or do we leave that now to the users or only do it in LwM2MClient.stop()?
		LOG.info("Got Deregister Request response. " + coapResponse.getCode());
		if (coapResponse == null) {
			lwM2mresponse = OperationResponse.failure(ResponseCode.GATEWAY_TIMEOUT, "Timed Out Waiting For Response.");
		} else if (ResponseCode.isSuccess(coapResponse.getCode())) {
			lwM2mresponse = OperationResponse.of(coapResponse);
		} else {
			lwM2mresponse = OperationResponse.failure(coapResponse.getCode(),
					"Request Failed on Server " + coapResponse.getOptions());
		}
	}

	@SuppressWarnings("unchecked")
	public OperationResponse getResponse() {
		return lwM2mresponse;
	}

}
