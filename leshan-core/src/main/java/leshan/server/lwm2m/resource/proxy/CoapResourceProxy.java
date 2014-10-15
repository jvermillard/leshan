package leshan.server.lwm2m.resource.proxy;

public interface CoapResourceProxy {

	void initialize(String resourceName);

	void setResourceType(String resourceType);

}
