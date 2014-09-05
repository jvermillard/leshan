package leshan.server.client;

import static org.junit.Assert.assertEquals;
import leshan.server.lwm2m.client.LinkObject;
import leshan.server.lwm2m.linkformat.LinkFormatParser;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ResponseCode;

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

		sendCreate(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		assertLinkFormatResponse(sendDiscover(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.CONTENT, client.getObjectModel(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID));
	}

	@Test
	public void testDiscoverObjectAndObjectInstanceAndResource() {
		register();

		sendCreate(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		assertLinkFormatResponse(sendDiscover(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID), ResponseCode.CONTENT, client.getObjectModel(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID));
	}

	@Test
	public void testCantDiscoverNonExistentObjectAndObjectInstanceAndResource() {
		register();

		sendCreate(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

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
