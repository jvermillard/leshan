package leshan.client.californium.impl;

import org.eclipse.californium.core.coap.Request;

public interface CoapRequestVisitor {
	
	public void accept(Request coapRequest);
}
