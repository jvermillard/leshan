package leshan.server.lwm2m.resource;

import leshan.server.lwm2m.resource.proxy.CoapResourceProxy;

import org.apache.commons.lang.Validate;

public abstract class CoapResource {

	private final CoapResourceProxy coapResourceProxy;

	public CoapResource(final CoapResourceProxy factory, final String resourceName) {
		this.coapResourceProxy = factory;
		coapResourceProxy.initialize(resourceName);
	}
	
	public CoapResourceProxy getCoapResourceProxy() {
		Validate.notNull(coapResourceProxy);
		
		return coapResourceProxy;
	}

}
