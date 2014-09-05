package leshan.client.lwm2m.resource;

public class LwM2mObjectDefinition {

	private final int id;
	private final LwM2mResourceDefinition[] definitions;

	public LwM2mObjectDefinition(final int objectId, final LwM2mResourceDefinition... definitions) {
		this.id = objectId;
		this.definitions = definitions;
	}

	public int getId() {
		return id;
	}

	public LwM2mResourceDefinition[] getDefinitions() {
		return definitions;
	}

}
