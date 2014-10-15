package leshan.server.lwm2m.resource.californium;

import leshan.server.lwm2m.resource.CoapResource;
import leshan.server.lwm2m.resource.proxy.CoapResourceProxy;

import org.apache.commons.lang.Validate;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

public class CaliforniumCoapResourceProxy implements CoapResourceProxy{

	private ProxyCoapResource proxyCoapResource;
	private CoapResource parentResource;

	@Override
	public void initialize(final CoapResource parent, final String resourceName) {
		this.proxyCoapResource = new ProxyCoapResource(resourceName);
		this.parentResource = parent;
	}
	
	@Override
	public void setResourceType(final String resourceType) {
		Validate.notNull(proxyCoapResource);
		
		this.proxyCoapResource.getAttributes().addResourceType(resourceType);
	}
	
	public Resource getCoapResource() {
		return proxyCoapResource;
	}
	
	private class ProxyCoapResource extends org.eclipse.californium.core.CoapResource{

		public ProxyCoapResource(final String resourceName) {
			super(resourceName);
		}
		
		@Override
		public void handlePOST(final CoapExchange exchange) {
			parentResource.onPOST(new CaliforniumExchangeProxy(exchange));
		}
		
	}

}
