package leshan.server.lwm2m.resource.proxy;

import org.apache.commons.lang.Validate;

public class CaliforniumCoapResourceProxy implements CoapResourceProxy{

	private ProxyCoapResource proxyCoapResource;

	@Override
	public void initialize(final String resourceName) {
		this.proxyCoapResource = new ProxyCoapResource(resourceName);
	}
	
	@Override
	public void setResourceType(final String resourceType) {
		Validate.notNull(proxyCoapResource);
		
		this.proxyCoapResource.getAttributes().addResourceType(resourceType);
	}
	
	private class ProxyCoapResource extends org.eclipse.californium.core.CoapResource{

		public ProxyCoapResource(final String resourceName) {
			super(resourceName);
		}
		
	}

}
