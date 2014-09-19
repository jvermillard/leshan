package leshan.server.client;

import static leshan.server.lwm2m.message.ResponseCode.CONTENT;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import leshan.server.lwm2m.message.ClientResourceSpec;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.observation.ObserveSpec;
import leshan.server.lwm2m.observation.ResourceObserver;
import leshan.server.lwm2m.tlv.Tlv;

import org.junit.Ignore;
import org.junit.Test;

import com.jayway.awaitility.Awaitility;

public class ObserveTest extends LwM2mClientServerIntegrationTest {

	private static final String HELLO = "hello";
	private static final String GOODBYE = "goodbye";
	private static final String WORLD = "world";
	private final Observer observer = new Observer();

	@Test
	public void canObserveResource() {
		register();

		sendCreate(createGoodResourcesTlv(HELLO, GOODBYE), GOOD_OBJECT_ID);

		final ClientResponse response = sendObserve(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID, observer);
		assertResponse(response, CONTENT, HELLO.getBytes());

		firstResource.setValue(WORLD);
		Awaitility.await().untilTrue(observer.receievedNotify());
		assertArrayEquals(WORLD.getBytes(), observer.getContent());
	}

	@Test
	public void canObserveIntResource() {
		register();

		sendCreate(new Tlv[0], INT_OBJECT_ID);

		final ClientResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID, observer);
		assertResponse(response, CONTENT, "0".getBytes());

		intResource.setValue(2);
		Awaitility.await().untilTrue(observer.receievedNotify());
		assertArrayEquals("2".getBytes(), observer.getContent());
	}

	@Ignore
	@Test
	public void canObserveIntResourceWithGtAttributeWithNotify() {
		register();

		sendCreate(new Tlv[0], INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().greaterThan(6).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ClientResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID, observer);
		assertResponse(response, CONTENT, "0".getBytes());

		intResource.setValue(20);
		Awaitility.await().untilTrue(observer.receievedNotify());
		assertArrayEquals("20".getBytes(), observer.getContent());
	}

	@Ignore
	@Test
	public void canObserveIntResourceWithGtAttributeNoNotify() {
		register();

		sendCreate(new Tlv[0], INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().greaterThan(6).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ClientResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID, observer);
		assertResponse(response, CONTENT, "0".getBytes());

		intResource.setValue(2);
		sleep(500);
		assertFalse(observer.receievedNotify().get());
	}

	@Ignore
	@Test
	public void canObserveIntResourceWithLtAttributeWithNotify() {
		register();

		sendCreate(new Tlv[0], INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().lessThan(6).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ClientResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID, observer);
		assertResponse(response, CONTENT, "0".getBytes());

		intResource.setValue(2);
		Awaitility.await().untilTrue(observer.receievedNotify());
		assertArrayEquals("2".getBytes(), observer.getContent());
	}

	@Ignore
	@Test
	public void canObserveIntResourceWithLtAttributeNoNotify() {
		register();

		sendCreate(new Tlv[0], INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().lessThan(6).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ClientResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID, observer);
		assertResponse(response, CONTENT, "0".getBytes());

		intResource.setValue(20);
		sleep(500);
		assertFalse(observer.receievedNotify().get());
	}

	@Ignore
	@Test
	public void canObserveIntResourceWithGtAndLtAttributeWithNotify() {
		register();

		sendCreate(new Tlv[0], INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().greaterThan(10).lessThan(6).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ClientResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID, observer);
		assertResponse(response, CONTENT, "0".getBytes());

		intResource.setValue(20);
		Awaitility.await().untilTrue(observer.receievedNotify());
		assertArrayEquals("20".getBytes(), observer.getContent());
	}

	@Test
	public void canObserveResourceWithPmaxAttributeWithNotify() {
		register();

		sendCreate(new Tlv[0], INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().maxPeriod(2).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ClientResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID, observer);
		assertResponse(response, CONTENT, "0".getBytes());

		sleep(3000);
		assertTrue(observer.receievedNotify().get());
		assertArrayEquals("0".getBytes(), observer.getContent());
	}

	@Test
	public void canObserveResourceWithPmaxAttributeNoNotify() {
		register();

		sendCreate(new Tlv[0], INT_OBJECT_ID);

		sendWriteAttributes(new ObserveSpec.Builder().maxPeriod(2).build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);

		final ClientResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID, observer);
		assertResponse(response, CONTENT, "0".getBytes());

		sleep(1000);
		assertFalse(observer.receievedNotify().get());
	}

	private void sleep(final long time) {
		try {
			Thread.sleep(time);
		} catch (final InterruptedException e) {
		}
	}

	private class Observer implements ResourceObserver {

		private byte[] content;
		private final AtomicBoolean notified = new AtomicBoolean(false);

		@Override
		public void notify(final byte[] content, final ContentFormat contentFormat,
				final ClientResourceSpec target) {
			this.content = content;
			notified.set(true);
		}

		public AtomicBoolean receievedNotify() {
			return notified;
		}

		public byte[] getContent() {
			return content;
		}

	}

}
