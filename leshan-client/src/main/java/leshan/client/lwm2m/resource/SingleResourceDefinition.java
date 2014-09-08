package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.LwM2mResponse;

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

	//TODO This should be replaced with calls to write()
	@Override
	public LwM2mResource createResource(final byte[] value) {
		resource.write(new LwM2mExchange() {

			@Override
			public void respond(final LwM2mResponse response) {
			}

			@Override
			public byte[] getRequestPayload() {
				return value;
			}

			@Override
			public boolean hasObjectInstanceId() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public int getObjectInstanceId() {
				// TODO Auto-generated method stub
				return 0;
			}

		});
		return resource;
	}

}
