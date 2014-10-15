package leshan.server.lwm2m.resource.proxy;

import leshan.server.lwm2m.resource.CoapResource;

public interface CoapResourceProxy {

	void initialize(CoapResource coapResource, String resourceName);

	void setResourceType(String resourceType);

}
