package leshan.server.lwm2m.resource;

import leshan.server.lwm2m.resource.proxy.CoapResourceProxy;
import leshan.server.lwm2m.resource.proxy.ExchangeProxy;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CoapResource {
	private static final Logger LOG = LoggerFactory.getLogger(CoapResource.class);

	private final CoapResourceProxy coapResourceProxy;

	public CoapResource(final CoapResourceProxy factory, final String resourceName) {
		this.coapResourceProxy = factory;
		coapResourceProxy.initialize(this, resourceName);
	}
	
	public CoapResourceProxy getCoapResourceProxy() {
		Validate.notNull(coapResourceProxy);
		
		return coapResourceProxy;
	}

	public void handlePOST(final ExchangeProxy exchangeProxy){
		LOG.debug("Doing nothing by default.");
	}

	public void handlePUT(final ExchangeProxy exchangeProxy){
		LOG.debug("Doing nothing by default.");
	}

	public void handleDELETE(final ExchangeProxy exchangeProxy){
		LOG.debug("Doing nothing by default.");
	}
}
