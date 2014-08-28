package leshan.client.lwm2m.resource;

public class ExecutableResourceDefinition implements ClientResourceDefinition {

	private final int id;
	private final ExecuteListener listener;

	public ExecutableResourceDefinition(final int id, final ExecuteListener listener) {
		this.id = id;
		this.listener = listener;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public ClientResource createResource() {
		return new ExecutableResource(id, listener);
	}

}
