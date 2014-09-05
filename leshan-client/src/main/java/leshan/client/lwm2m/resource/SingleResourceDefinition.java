package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mResource;

public class SingleResourceDefinition implements LwM2mResourceDefinition {

	private final int id;
	private final LwM2mResource resource;

	public SingleResourceDefinition(final int id, final LwM2mResource resource) {
		this.id = id;
		this.resource = resource;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public LwM2mResource createResource() {
		return resource;
	}

}
