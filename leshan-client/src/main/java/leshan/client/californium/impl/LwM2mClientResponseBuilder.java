package leshan.client.californium.impl;

import java.util.logging.Logger;

import leshan.client.request.BootstrapRequest;
import leshan.client.request.DeregisterRequest;
import leshan.client.request.LwM2mClientRequest;
import leshan.client.request.LwM2mClientRequestVisitor;
import leshan.client.request.RegisterRequest;
import leshan.client.request.UpdateRequest;
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
	private final CaliforniumClientIdentifierBuilder californiumClientIdentifierBuilder;

	public LwM2mClientResponseBuilder(final Request coapRequest,
			final Response coapResponse, final CaliforniumLwM2mClientRequestSender californiumLwM2mClientRequestSender) {
		this.coapRequest = coapRequest;
		this.coapResponse = coapResponse;
		this.californiumLwM2mClientRequestSender = californiumLwM2mClientRequestSender;
		this.californiumClientIdentifierBuilder = new CaliforniumClientIdentifierBuilder(coapResponse);
	}

	@Override
	public void visit(final RegisterRequest request) {
		//TODO run this through the eclipse stylesheet
		buildClientIdentifier(request);
		buildResponse();
	}

	@Override
	public void visit(final DeregisterRequest request) {
		buildClientIdentifier(request);
		buildResponse();
	}
	
	@Override
	public void visit(final UpdateRequest request) {
		buildClientIdentifier(request);
		buildResponse();
	}
	
	@Override
	public void visit(final BootstrapRequest request) {
		buildClientIdentifier(request);
		buildResponse();
	}
	
	private void buildClientIdentifier(final LwM2mClientRequest request) {
		request.accept(californiumClientIdentifierBuilder);
	}
	
	private void buildResponse() {
		if (coapResponse == null) {
			lwM2mresponse = OperationResponse.failure(ResponseCode.GATEWAY_TIMEOUT, "Timed Out Waiting For Response.");
		} else if (ResponseCode.isSuccess(coapResponse.getCode())) {
			lwM2mresponse = OperationResponse.of(coapResponse, californiumClientIdentifierBuilder.getClientIdentifier());
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
