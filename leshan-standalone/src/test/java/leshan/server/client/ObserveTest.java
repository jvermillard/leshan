package leshan.server.client;

import leshan.server.lwm2m.node.LwM2mObjectInstance;
import leshan.server.lwm2m.node.LwM2mResource;
import leshan.server.lwm2m.node.Value;
import leshan.server.lwm2m.observation.ObserveSpec;
import leshan.server.lwm2m.request.ResponseCode;
import leshan.server.lwm2m.request.ValueResponse;

import org.junit.Ignore;
import org.junit.Test;

public class ObserveTest extends LwM2mClientServerIntegrationTest {

	private static final String HELLO = "hello";
	private static final String GOODBYE = "goodbye";
	private static final String WORLD = "world";

	@Test
	public void canObserveResource() {
		register();

		sendCreate(createGoodObjectInstance(HELLO, GOODBYE), GOOD_OBJECT_ID);

		final ValueResponse response = sendObserve(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue(HELLO)));

		firstResource.setValue(WORLD);
//		Awaitility.await().untilTrue(observer.receievedNotify());
//		assertArrayEquals(WORLD.getBytes(), observer.getContent());
	}

	@Test
	public void canObserveIntResource() {
		register();

		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);

		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newIntegerValue(0)));

		intResource.setValue(2);
//		Awaitility.await().untilTrue(observer.receievedNotify());
//		assertArrayEquals("2".getBytes(), observer.getContent());
	}

	@Ignore
	@Test
	public void canObserveIntResourceWithGtAttributeWithNotify() {
		register();

		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().greaterThan(6).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newIntegerValue(0)));

		intResource.setValue(20);
//		Awaitility.await().untilTrue(observer.receievedNotify());
//		assertArrayEquals("20".getBytes(), observer.getContent());
	}

	@Ignore
	@Test
	public void canObserveIntResourceWithGtAttributeNoNotify() {
		register();

		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().greaterThan(6).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newIntegerValue(0)));

		intResource.setValue(2);
//		sleep(500);
//		assertFalse(observer.receievedNotify().get());
	}

	@Ignore
	@Test
	public void canObserveIntResourceWithLtAttributeWithNotify() {
		register();

		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().lessThan(6).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newIntegerValue(0)));

		intResource.setValue(2);
//		Awaitility.await().untilTrue(observer.receievedNotify());
//		assertArrayEquals("2".getBytes(), observer.getContent());
	}

	@Ignore
	@Test
	public void canObserveIntResourceWithLtAttributeNoNotify() {
		register();

		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().lessThan(6).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newIntegerValue(0)));

		intResource.setValue(20);
//		sleep(500);
//		assertFalse(observer.receievedNotify().get());
	}

	@Ignore
	@Test
	public void canObserveIntResourceWithGtAndLtAttributeWithNotify() {
		register();

		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().greaterThan(10).lessThan(6).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newIntegerValue(0)));

		intResource.setValue(20);
//		Awaitility.await().untilTrue(observer.receievedNotify());
//		assertArrayEquals("20".getBytes(), observer.getContent());
	}

	@Test
	public void canObserveResourceWithPmaxAttributeWithNotify() {
		register();

		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().maxPeriod(2).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newIntegerValue(0)));

		sleep(3000);
//		assertTrue(observer.receievedNotify().get());
//		assertArrayEquals("0".getBytes(), observer.getContent());
	}

	@Test
	public void canObserveResourceWithPmaxAttributeNoNotify() {
		register();

		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().maxPeriod(2).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newIntegerValue(0)));

//		sleep(1000);
//		assertFalse(observer.receievedNotify().get());
	}

	private void sleep(final long time) {
		try {
			Thread.sleep(time);
		} catch (final InterruptedException e) {
		}
	}

}
