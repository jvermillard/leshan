package leshan.server.client;

import java.util.HashMap;
import java.util.Map;

import leshan.server.lwm2m.node.LwM2mNode;
import leshan.server.lwm2m.node.LwM2mObject;
import leshan.server.lwm2m.node.LwM2mObjectInstance;
import leshan.server.lwm2m.node.LwM2mResource;
import leshan.server.lwm2m.node.Value;
import leshan.server.lwm2m.request.ResponseCode;

import org.junit.Test;

public class ReadTest extends LwM2mClientServerIntegrationTest {

	private static final String HELLO = "hello";
	private static final String GOODBYE = "goodbye";

	@Test
	public void canReadObject() {
		register();
		assertEmptyResponse(sendRead(GOOD_OBJECT_ID), ResponseCode.CONTENT);
	}

	@Test
	public void canReadObjectWithCreatedInstance() {
		register();
		sendCreate(createGoodObjectInstance(HELLO, GOODBYE), GOOD_OBJECT_ID);

		LwM2mNode objectNode = new LwM2mObject(GOOD_OBJECT_ID, new LwM2mObjectInstance[] {
				new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] {
						new LwM2mResource(FIRST_RESOURCE_ID, Value.newStringValue(HELLO)),
						new LwM2mResource(SECOND_RESOURCE_ID, Value.newStringValue(GOODBYE))
				})	
		});
		assertResponse(sendRead(GOOD_OBJECT_ID), ResponseCode.CONTENT, objectNode);
	}

	@Test
	public void canReadObjectInstance() {
		register();
		sendCreate(createGoodObjectInstance(HELLO, GOODBYE), GOOD_OBJECT_ID);

		LwM2mObjectInstance objectInstanceNode = new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] {
				new LwM2mResource(FIRST_RESOURCE_ID, Value.newStringValue(HELLO)),
				new LwM2mResource(SECOND_RESOURCE_ID, Value.newStringValue(GOODBYE))
		});
		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.CONTENT, objectInstanceNode);
	}

	@Test
	public void canReadResource() {
		register();
		sendCreate(createGoodObjectInstance(HELLO, GOODBYE), GOOD_OBJECT_ID);

		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID),
				ResponseCode.CONTENT, new LwM2mResource(FIRST_RESOURCE_ID, Value.newStringValue(HELLO)));
		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
				ResponseCode.CONTENT, new LwM2mResource(SECOND_RESOURCE_ID, Value.newStringValue(GOODBYE)));
	}

	@Test
	public void cannotReadNonReadableResource() {
		register();
		sendCreate(createGoodObjectInstance(HELLO, GOODBYE), GOOD_OBJECT_ID);

		assertEmptyResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, EXECUTABLE_RESOURCE_ID), ResponseCode.METHOD_NOT_ALLOWED);
	}

	@Test
	public void cannotReadNonExistentResource() {
		register();
		sendCreate(createGoodObjectInstance(HELLO, GOODBYE), GOOD_OBJECT_ID);

		assertEmptyResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INVALID_RESOURCE_ID), ResponseCode.NOT_FOUND);
	}

	@Test
	public void canReadMultipleResource() {
		register();
		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), MULTIPLE_OBJECT_ID);

		final Map<Integer, byte[]> map = new HashMap<Integer, byte[]>();
		map.put(0, HELLO.getBytes());
		map.put(1, GOODBYE.getBytes());
		multipleResource.setValue(map);

		LwM2mNode resource = new LwM2mResource(MULTIPLE_RESOURCE_ID, new Value<?>[] {
				Value.newStringValue(HELLO),
				Value.newStringValue(GOODBYE)		
		});

		assertResponse(sendRead(MULTIPLE_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, MULTIPLE_RESOURCE_ID),
				ResponseCode.CONTENT, resource);
	}


	// TODO: This doesn't seem like it should be passing. Figure out why it is.
	// Expected response TLV type to be RESOURCE_VALUE
	@Test
	public void canReadObjectInstanceWithMultipleResource() {
		register();
		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), MULTIPLE_OBJECT_ID);

		final Map<Integer, byte[]> map = new HashMap<Integer, byte[]>();
		map.put(0, HELLO.getBytes());
		map.put(1, GOODBYE.getBytes());

		multipleResource.setValue(map);

		LwM2mObjectInstance objectInstance = new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] {
				new LwM2mResource(MULTIPLE_RESOURCE_ID, new Value<?>[] {
						Value.newStringValue(HELLO),
						Value.newStringValue(GOODBYE)		
				})
		});

		assertResponse(sendRead(MULTIPLE_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.CONTENT, objectInstance);
	}

}
