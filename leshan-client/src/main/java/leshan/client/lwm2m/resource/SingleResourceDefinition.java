package leshan.client.lwm2m.resource;

public class SingleResourceDefinition implements ClientResourceDefinition {

	private final int id;
	private final ExecuteListener listener;

	public SingleResourceDefinition(final int id, final ExecuteListener listener) {
		this.id = id;
		this.listener = listener;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public ClientResource createResource() {
		return new ClientResource(id, listener);
	}

}
