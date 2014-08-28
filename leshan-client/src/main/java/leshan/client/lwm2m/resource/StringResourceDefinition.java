package leshan.client.lwm2m.resource;

public class StringResourceDefinition implements ClientResourceDefinition {

	private final Integer id;

	public StringResourceDefinition(final int id) {
		this.id = id;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public ClientResource createResource() {
		return new ClientResource(id, null);
	}

}
