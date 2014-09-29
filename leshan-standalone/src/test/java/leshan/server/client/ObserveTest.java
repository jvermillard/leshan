package leshan.server.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import leshan.server.lwm2m.node.LwM2mNode;
import leshan.server.lwm2m.node.LwM2mObjectInstance;
import leshan.server.lwm2m.node.LwM2mResource;
import leshan.server.lwm2m.node.Value;
import leshan.server.lwm2m.observation.Observation;
import leshan.server.lwm2m.observation.ObservationRegistryListener;
import leshan.server.lwm2m.observation.ObserveSpec;
import leshan.server.lwm2m.request.ResponseCode;
import leshan.server.lwm2m.request.ValueResponse;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.awaitility.Awaitility;

public class ObserveTest extends LwM2mClientServerIntegrationTest {

	private static final String HELLO = "hello";
	private static final String GOODBYE = "goodbye";
	private static final String WORLD = "world";
	private SampleObservation observer;

	@Before
	public void setupObservation() {
		observer = new SampleObservation();
		observationRegistry.addListener(observer);
	}

	@Test
	public void canObserveResource() {
		register();

		sendCreate(createGoodObjectInstance(HELLO, GOODBYE), GOOD_OBJECT_ID);

		final ValueResponse response = sendObserve(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(FIRST_RESOURCE_ID, Value.newStringValue(HELLO)));

		firstResource.setValue(WORLD);

		Awaitility.await().untilTrue(observer.receievedNotify());
		assertEquals(new LwM2mResource(FIRST_RESOURCE_ID, Value.newStringValue(WORLD)), observer.getContent());
	}

	@Test
	public void canObserveIntResource() {
		register();

		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);

		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue("0")));

		intResource.setValue(2);
		Awaitility.await().untilTrue(observer.receievedNotify());
		assertEquals(new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue("2")), observer.getContent());
	}

	@Ignore
	@Test
	public void canObserveIntResourceWithGtAttributeWithNotify() {
		register();

		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().greaterThan(6).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue("0")));

		intResource.setValue(20);
		Awaitility.await().untilTrue(observer.receievedNotify());
		assertEquals(new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue("20")), observer.getContent());
	}

	@Ignore
	@Test
	public void canObserveIntResourceWithGtAttributeNoNotify() {
		register();

		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().greaterThan(6).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue("0")));

		intResource.setValue(2);
		sleep(500);
		assertFalse(observer.receievedNotify().get());
	}

	@Ignore
	@Test
	public void canObserveIntResourceWithLtAttributeWithNotify() {
		register();

		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().lessThan(6).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue("0")));

		intResource.setValue(2);
		Awaitility.await().untilTrue(observer.receievedNotify());
		assertEquals(new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue("2")), observer.getContent());
	}

	@Ignore
	@Test
	public void canObserveIntResourceWithLtAttributeNoNotify() {
		register();

		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().lessThan(6).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue("0")));

		intResource.setValue(20);
		sleep(500);
		assertFalse(observer.receievedNotify().get());
	}

	@Ignore
	@Test
	public void canObserveIntResourceWithGtAndLtAttributeWithNotify() {
		register();

		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().greaterThan(10).lessThan(6).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue("0")));

		intResource.setValue(20);
		Awaitility.await().untilTrue(observer.receievedNotify());
		assertEquals(new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue("20")), observer.getContent());
	}

	@Test
	public void canObserveResourceWithPmaxAttributeWithNotify() {
		register();

		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().maxPeriod(2).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue("0")));

		sleep(3000);
		assertTrue(observer.receievedNotify().get());
		assertEquals(new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue("0")), observer.getContent());
	}

	@Test
	public void canObserveResourceWithPmaxAttributeNoNotify() {
		register();

		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().maxPeriod(2).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue("0")));

		sleep(1000);
		assertFalse(observer.receievedNotify().get());
	}

	private void sleep(final long time) {
		try {
			Thread.sleep(time);
		} catch (final InterruptedException e) {
		}
	}

	private final class SampleObservation implements ObservationRegistryListener {
		private AtomicBoolean receivedNotify = new AtomicBoolean();
		private LwM2mNode content;

		@Override
		public void newValue(Observation observation, LwM2mNode value) {
			System.out.println("NEW VALUE");
			receivedNotify.set(true);
			content = value;
		}

		@Override
		public void cancelled(Observation observation) {

		}

		@Override
		public void newObservation(Observation observation) {

		}

		public AtomicBoolean receievedNotify() {
			return receivedNotify;
		}

		public LwM2mNode getContent() {
			return content;
		}
	}

}

