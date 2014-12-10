package leshan.client.californium.impl;

import leshan.client.request.BootstrapRequest;
import leshan.client.request.DeregisterRequest;
import leshan.client.request.LwM2mClientRequestVisitor;
import leshan.client.request.LwM2mIdentifierRequest;
import leshan.client.request.RegisterRequest;
import leshan.client.request.UpdateRequest;
import leshan.client.request.identifier.ClientIdentifier;

import org.eclipse.californium.core.coap.Response;

public class CaliforniumClientIdentifierBuilder implements LwM2mClientRequestVisitor {

	private ClientIdentifier clientIdentifier;
	private final Response coapResponse;

	public CaliforniumClientIdentifierBuilder(final Response coapResponse) {
		this.coapResponse = coapResponse;
	}

	@Override
	public void visit(final RegisterRequest request) {
		buildClientIdentifier(request);
	}

	@Override
	public void visit(final DeregisterRequest request) {
		clientIdentifier = request.getClientIdentifier();
	}

	@Override
	public void visit(final UpdateRequest request) {
		clientIdentifier = request.getClientIdentifier();
	}

	@Override
	public void visit(final BootstrapRequest request) {
		// TODO Auto-generated method stub
		
	}
	
	private void buildClientIdentifier(final LwM2mIdentifierRequest request) {
		clientIdentifier = new CaliforniumClientIdentifier(coapResponse.getOptions().getLocationString(), request.getClientEndpointIdentifier());
	}

	public ClientIdentifier getClientIdentifier() {
		return clientIdentifier;
	}

}
