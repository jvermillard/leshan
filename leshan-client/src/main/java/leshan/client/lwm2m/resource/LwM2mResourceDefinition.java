package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mResource;

public interface LwM2mResourceDefinition {

	public int getId();
	public LwM2mResource createResource();
	public LwM2mResource createResource(byte[] value);

}
