package leshan.client.lwm2m.resource;


public class SingleResourceDefinition implements LwM2mResourceDefinition {

	private final int id;
	private final LwM2mResource resource;

	public SingleResourceDefinition(final int id, final LwM2mResource resource) {
		this.id = id;
		this.resource = resource;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public LwM2mResource createResource() {
		return resource;
	}

}
