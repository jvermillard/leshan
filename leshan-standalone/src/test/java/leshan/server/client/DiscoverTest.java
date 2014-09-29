package leshan.server.client;

import static org.junit.Assert.assertEquals;
import leshan.server.lwm2m.client.LinkObject;
import leshan.server.lwm2m.request.DiscoverResponse;
import leshan.server.lwm2m.request.ResponseCode;

import org.junit.Test;

public class DiscoverTest extends LwM2mClientServerIntegrationTest {

	@Test
	public void testDiscoverObject() {
		register();

		final DiscoverResponse response = sendDiscover(GOOD_OBJECT_ID);
		assertLinkFormatResponse(response, ResponseCode.CONTENT, client.getObjectModel(GOOD_OBJECT_ID));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testIllegalObjectLinksRequestTwo() {
		register();

		final DiscoverResponse response = sendDiscover(GOOD_OBJECT_ID);
		assertLinkFormatResponse(response, ResponseCode.CONTENT, client.getObjectModel(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID, SECOND_RESOURCE_ID));
	}

	@Test
	public void testDiscoverObjectInstance() {
		register();

		sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

		assertLinkFormatResponse(sendDiscover(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.CONTENT, client.getObjectModel(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID));
	}

	@Test
	public void testDiscoverResource() {
		register();

		sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

		assertLinkFormatResponse(sendDiscover(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID), ResponseCode.CONTENT, client.getObjectModel(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID));
	}

	@Test
	public void testCantDiscoverNonExistentResource() {
		register();

		sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

		assertEmptyResponse(sendDiscover(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, 1234231), ResponseCode.NOT_FOUND);
	}

	private void assertLinkFormatResponse(final DiscoverResponse response,
			final ResponseCode responseCode, final LinkObject[] expectedObjects) {
		assertEquals(responseCode, response.getCode());

		final LinkObject[] actualObjects = response.getObjectLinks();

		assertEquals(expectedObjects.length, actualObjects.length);
		for(int i = 0; i < expectedObjects.length; i++){
			assertEquals(expectedObjects[i].toString(), actualObjects[i].toString());
		}
	}

}
