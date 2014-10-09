package leshan.server.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import leshan.server.lwm2m.node.LwM2mNode;
import leshan.server.lwm2m.node.LwM2mObject;
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

	private SampleObservation observer;

	@Before
	public void setupObservation() {
		observer = new SampleObservation();
		observationRegistry.addListener(observer);

		register();
		create();
	}

	@Test
	public void canObserveResource() {
		observeResource();

		intResource.setValue(2);
		assertObservedResource("2");
	}

	@Ignore
	@Test
	public void canObserveResourceWithGtAttributeWithNotify() {
		observeResource(attributes().greaterThan(6));

		intResource.setValue(20);
		assertObservedResource("20");
	}

	@Ignore
	@Test
	public void canObserveResourceWithGtAttributeNoNotify() {
		observeResource(attributes().greaterThan(6));

		intResource.setValue(2);
		assertNoObservation(500);
	}

	@Ignore
	@Test
	public void canObserveResourceWithLtAttributeWithNotify() {
		observeResource(attributes().lessThan(6));

		intResource.setValue(2);
		assertObservedResource("2");
	}

	@Ignore
	@Test
	public void canObserveResourceWithLtAttributeNoNotify() {
		observeResource(attributes().lessThan(6));

		intResource.setValue(20);
		assertNoObservation(500);
	}

	@Ignore
	@Test
	public void canObserveResourceWithGtAndLtAttributeWithNotify() {
		observeResource(attributes().greaterThan(10).lessThan(6));

		intResource.setValue(20);
		assertObservedResource("20");
	}

	@Test
	public void canObserveResourceWithPmaxAttributeWithNotify() {
		observeResource(attributes().maxPeriod(1));

		assertObservedResource(2000, "0");
	}

	@Test
	public void canObserveResourceWithPmaxAttributeNoNotify() {
		observeResource(attributes().maxPeriod(1));

		assertNoObservation(500);
	}

	@Test
	public void canObserveObjectInstanceWithPmaxWithNotify() {
		observeObjectInstance(attributes().maxPeriod(1));

		intResource.setValue(2);
		assertObservedObjectInstance(2000, "2");
	}

	@Test
	public void canObserveObjectWithPmaxWithNotify() {
		observeObject(attributes().maxPeriod(1));

		intResource.setValue(2);
		assertObservedObject(2000, "2");
	}

	private void create() {
		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);
	}

	private ObserveSpec.Builder attributes() {
		return new ObserveSpec.Builder();
	}

	private void observeResource() {
		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue("0")));
	}

	private void observeResource(ObserveSpec.Builder observeSpecBuilder) {
		sendWriteAttributes(observeSpecBuilder.build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		observeResource();
	}

	private void observeObjectInstance() {
		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] {
				new LwM2mResource(INT_RESOURCE_ID, Value.newBinaryValue("0".getBytes()))
		}));
	}

	private void observeObjectInstance(ObserveSpec.Builder observeSpecBuilder) {
		sendWriteAttributes(observeSpecBuilder.build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID);
		observeObjectInstance();
	}

	private void observeObject() {
		final ValueResponse response = sendObserve(INT_OBJECT_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mObject(INT_OBJECT_ID, new LwM2mObjectInstance[] {
				new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] {
						new LwM2mResource(INT_RESOURCE_ID, Value.newBinaryValue("0".getBytes()))		
				})
		}));
	}

	private void observeObject(ObserveSpec.Builder observeSpecBuilder) {
		sendWriteAttributes(observeSpecBuilder.build(), INT_OBJECT_ID);
		observeObject();
	}

	private void assertObservedResource(String value) {
		assertObservedResource(500, value);
	}

	private void assertObservedResource(long timeoutInSeconds, String value) {
		Awaitility.await().atMost(timeoutInSeconds, TimeUnit.MILLISECONDS).untilTrue(observer.receievedNotify());
		assertEquals(new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue(value)), observer.getContent());
	}

	private void assertObservedObjectInstance(long timeoutInSeconds, String resourceValue) {
		Awaitility.await().atMost(timeoutInSeconds, TimeUnit.MILLISECONDS).untilTrue(observer.receievedNotify());
		assertEquals(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] {
				new LwM2mResource(INT_RESOURCE_ID, Value.newBinaryValue(resourceValue.getBytes()))		
		}), observer.getContent());
	}

	private void assertObservedObject(long timeoutInSeconds, String resourceValue) {
		Awaitility.await().atMost(timeoutInSeconds, TimeUnit.MILLISECONDS).untilTrue(observer.receievedNotify());
		assertEquals(new LwM2mObject(INT_OBJECT_ID, new LwM2mObjectInstance[] {
				new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] {
						new LwM2mResource(INT_RESOURCE_ID, Value.newBinaryValue(resourceValue.getBytes()))		
				})
		}), observer.getContent());
	}

	private void assertNoObservation(long time) {
		sleep(time);
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

