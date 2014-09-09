package leshan.client.lwm2m.resource;

public class SingleResourceDefinition implements LwM2mResourceDefinition {

	private final int id;
	private final LwM2mResource resource;
	private final boolean required;
	private final boolean writable;

	public SingleResourceDefinition(final int id, final LwM2mResource resource, final boolean required, final boolean writable) {
		this.id = id;
		this.resource = resource;
		this.required = required;
		this.writable = writable;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public LwM2mResource createResource() {
		return resource;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean isWritable() {
		return writable;
	}

}
