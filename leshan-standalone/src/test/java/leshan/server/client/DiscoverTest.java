package leshan.server.client;

import static org.junit.Assert.*;
import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.operation.Executable;
import leshan.client.lwm2m.operation.Readable;
import leshan.client.lwm2m.operation.Writable;
import leshan.client.lwm2m.resource.ClientObject;
import leshan.client.lwm2m.resource.SingleResourceDefinition;
import leshan.server.lwm2m.client.LinkObject;
import leshan.server.lwm2m.linkformat.LinkFormatParser;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ResponseCode;

import org.junit.Test;

public class DiscoverTest extends LwM2mClientServerIntegrationTest {
	
	@Override
	protected LwM2mClient createClient() {
		final ReadWriteListenerWithBrokenWrite brokenResourceListener = new ReadWriteListenerWithBrokenWrite();
		
		final ClientObject objectOne = new ClientObject(GOOD_OBJECT_ID,
				new SingleResourceDefinition(FIRST_RESOURCE_ID, firstReadableWritable, firstReadableWritable, Executable.NOT_EXECUTABLE),
				new SingleResourceDefinition(SECOND_RESOURCE_ID, secondReadableWritable, secondReadableWritable, Executable.NOT_EXECUTABLE),
				new SingleResourceDefinition(EXECUTABLE_RESOURCE_ID, Readable.NOT_READABLE, Writable.NOT_WRITABLE, executableAlwaysSuccessful));
		final ClientObject objectTwo = new ClientObject(BROKEN_OBJECT_ID,
				new SingleResourceDefinition(BROKEN_RESOURCE_ID, brokenResourceListener, brokenResourceListener, Executable.NOT_EXECUTABLE));
		return new LwM2mClient(objectOne, objectTwo);
	}

	@Test
	public void testDiscoverObject() {
		register();
		
		final ClientResponse response = sendDiscover(GOOD_OBJECT_ID);
		assertLinkFormatResponse(response, ResponseCode.CONTENT, client.getObjectLinks(GOOD_OBJECT_ID));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalObjectLinksRequestTwo() {
		register();
		
		final ClientResponse response = sendDiscover(GOOD_OBJECT_ID);
		assertLinkFormatResponse(response, ResponseCode.CONTENT, client.getObjectLinks(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID, SECOND_RESOURCE_ID));
	}
	
	@Test
	public void testDiscoverObjectAndObjectInstance() {
		register();
		
		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);
		
		assertLinkFormatResponse(sendDiscover(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.CONTENT, client.getObjectLinks(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID));
	}
	
	@Test
	public void testDiscoverObjectAndObjectInstanceAndResource() {
		register();
		
		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);
		
		assertLinkFormatResponse(sendDiscover(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID), ResponseCode.CONTENT, client.getObjectLinks(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID));
	}
	
	@Test
	public void testCantDiscoverNonExistentObjectAndObjectInstanceAndResource() {
		register();
		
		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);
		
		assertEmptyResponse(sendDiscover(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, 1234231), ResponseCode.NOT_FOUND);
	}

	private void assertLinkFormatResponse(final ClientResponse response,
			final ResponseCode responseCode, final LinkObject[] expectedObjects) {
		assertEquals(responseCode, response.getCode());
		
		final LinkObject[] actualObjects = LinkFormatParser.parse(response.getContent());
		
		assertEquals(expectedObjects.length, actualObjects.length);
		for(int i = 0; i < expectedObjects.length; i++){
			assertEquals(expectedObjects[i].toString(), actualObjects[i].toString());
		}
	}

}
