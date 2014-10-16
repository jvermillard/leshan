package leshan.server.lwm2m.resource;

import leshan.server.lwm2m.resource.proxy.CoapResourceProxy;
import leshan.server.lwm2m.resource.proxy.ExchangeProxy;

import org.apache.commons.lang.Validate;

public abstract class CoapResource {

	private final CoapResourceProxy coapResourceProxy;

	public CoapResource(final CoapResourceProxy factory, final String resourceName) {
		this.coapResourceProxy = factory;
		coapResourceProxy.initialize(this, resourceName);
	}
	
	public CoapResourceProxy getCoapResourceProxy() {
		Validate.notNull(coapResourceProxy);
		
		return coapResourceProxy;
	}

	public abstract void handlePOST(ExchangeProxy exchangeProxy);
}
