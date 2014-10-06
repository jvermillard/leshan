package leshan.server.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.concurrent.TimeUnit;
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
		observe();

		intResource.setValue(2);
		assertObserved("2");
	}

	@Ignore
	@Test
	public void canObserveResourceWithGtAttributeWithNotify() {
		observe(attributes().greaterThan(6));

		intResource.setValue(20);
		assertObserved("20");
	}

	@Ignore
	@Test
	public void canObserveResourceWithGtAttributeNoNotify() {
		observe(attributes().greaterThan(6));

		intResource.setValue(2);
		assertNoObservation(500);
	}

	@Ignore
	@Test
	public void canObserveResourceWithLtAttributeWithNotify() {
		observe(attributes().lessThan(6));

		intResource.setValue(2);
		assertObserved("2");
	}

	@Ignore
	@Test
	public void canObserveResourceWithLtAttributeNoNotify() {
		observe(attributes().lessThan(6));

		intResource.setValue(20);
		assertNoObservation(500);
	}

	@Ignore
	@Test
	public void canObserveResourceWithGtAndLtAttributeWithNotify() {
		observe(attributes().greaterThan(10).lessThan(6));

		intResource.setValue(20);
		assertObserved("20");
	}

	@Test
	public void canObserveResourceWithPmaxAttributeWithNotify() {
		observe(attributes().maxPeriod(1));

		assertObserved(2000, "0");
	}

	@Test
	public void canObserveResourceWithPmaxAttributeNoNotify() {
		observe(attributes().maxPeriod(1));

		assertNoObservation(500);
	}

	private void create() {
		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);
	}

	private void observe() {
		final ValueResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue("0")));
	}

	private ObserveSpec.Builder attributes() {
		return new ObserveSpec.Builder();
	}

	private void observe(ObserveSpec.Builder observeSpecBuilder) {
		sendWriteAttributes(observeSpecBuilder.build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
		observe();
	}

	private void assertObserved(String value) {
		assertObserved(500, value);
	}

	private void assertObserved(long timeoutInSeconds, String value) {
		Awaitility.await().atMost(timeoutInSeconds, TimeUnit.MILLISECONDS).untilTrue(observer.receievedNotify());
		assertEquals(new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue(value)), observer.getContent());
	}

	private void assertNoObservation(int time) {
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

