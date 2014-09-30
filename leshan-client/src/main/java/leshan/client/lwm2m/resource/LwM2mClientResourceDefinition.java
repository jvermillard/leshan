package leshan.client.lwm2m.resource;

public interface LwM2mClientResourceDefinition {

	public int getId();
	public boolean isRequired();
	public LwM2mClientResource createResource();

}
