package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.Executable;
import leshan.client.lwm2m.operation.Readable;
import leshan.client.lwm2m.operation.Writable;

public class SingleResourceDefinition implements ClientResourceDefinition {

	private final int id;
	private final Executable executable;
	private final Writable writable;
	private final Readable readable;

	public SingleResourceDefinition(final int id, final Readable readable, final Writable writable, final Executable executable) {
		this.id = id;
		this.executable = executable;
		this.writable = writable;
		this.readable = readable;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public ClientResource createResource() {
		return new ClientResource(id, executable, writable, readable);
	}

}
