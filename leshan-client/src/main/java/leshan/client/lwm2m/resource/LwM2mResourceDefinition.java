package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mResource;

public interface LwM2mResourceDefinition {

	Integer getId();
	LwM2mResource createResource();

}
