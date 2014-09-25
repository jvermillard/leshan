package leshan.server.client;

import leshan.server.lwm2m.client.LinkObject;
import leshan.server.lwm2m.request.ClientResponse;
import leshan.server.lwm2m.request.ResponseCode;

import org.junit.Test;

public class DiscoverTest extends LwM2mClientServerIntegrationTest {

	@Test
	public void testDiscoverObject() {
		register();

		final ClientResponse response = sendDiscover(GOOD_OBJECT_ID);
		assertLinkFormatResponse(response, ResponseCode.CONTENT, client.getObjectModel(GOOD_OBJECT_ID));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testIllegalObjectLinksRequestTwo() {
		register();

		final ClientResponse response = sendDiscover(GOOD_OBJECT_ID);
		assertLinkFormatResponse(response, ResponseCode.CONTENT, client.getObjectModel(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID, SECOND_RESOURCE_ID));
	}

	@Test
	public void testDiscoverObjectAndObjectInstance() {
		register();

		sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

		assertLinkFormatResponse(sendDiscover(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.CONTENT, client.getObjectModel(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID));
	}

	@Test
	public void testDiscoverObjectAndObjectInstanceAndResource() {
		register();

		sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

		assertLinkFormatResponse(sendDiscover(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID), ResponseCode.CONTENT, client.getObjectModel(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID));
	}

	@Test
	public void testCantDiscoverNonExistentObjectAndObjectInstanceAndResource() {
		register();

		sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

		assertEmptyResponse(sendDiscover(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, 1234231), ResponseCode.NOT_FOUND);
	}

	private void assertLinkFormatResponse(final ClientResponse response,
			final ResponseCode responseCode, final LinkObject[] expectedObjects) {
		//FIXME: This needs to actually test something!
		//		assertEquals(responseCode, response.getCode());
		//
		//		final LinkObject[] actualObjects = LinkFormatParser.parse(response.getContent());
		//
		//		assertEquals(expectedObjects.length, actualObjects.length);
		//		for(int i = 0; i < expectedObjects.length; i++){
		//			assertEquals(expectedObjects[i].toString(), actualObjects[i].toString());
		//		}
	}

}
