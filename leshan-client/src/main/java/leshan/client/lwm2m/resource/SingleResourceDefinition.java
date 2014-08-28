package leshan.client.lwm2m.resource;

public class SingleResourceDefinition implements ClientResourceDefinition {

	private final int id;
	private final ExecuteListener executeListener;
	private final WriteListener writeListener;
	private final ReadListener readListener;

	public SingleResourceDefinition(final int id, final ExecuteListener executeListener, final WriteListener writeListener, final ReadListener readListener) {
		this.id = id;
		this.executeListener = executeListener;
		this.writeListener = writeListener;
		this.readListener = readListener;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public ClientResource createResource() {
		return new ClientResource(id, executeListener, writeListener, readListener);
	}

}
