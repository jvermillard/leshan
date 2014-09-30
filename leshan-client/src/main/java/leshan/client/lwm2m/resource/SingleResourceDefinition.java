package leshan.client.lwm2m.resource;

public class SingleResourceDefinition implements LwM2mClientResourceDefinition {

	private final int id;
	private final LwM2mClientResource resource;
	private final boolean required;

	public SingleResourceDefinition(final int id, final LwM2mClientResource resource, final boolean required) {
		this.id = id;
		this.resource = resource;
		this.required = required;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public LwM2mClientResource createResource() {
		return resource;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

}
