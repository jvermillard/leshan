package leshan.client.lwm2m.resource;

public interface LwM2mResourceDefinition {

	public int getId();
	public boolean isRequired();
	public LwM2mResource createResource();
	public boolean isWritable();

}
