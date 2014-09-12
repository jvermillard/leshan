package leshan.server.client;

import static leshan.server.lwm2m.message.ResponseCode.CONTENT;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;

import java.util.concurrent.atomic.AtomicBoolean;

import leshan.server.lwm2m.message.ClientResourceSpec;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.WriteAttributesRequest;
import leshan.server.lwm2m.observation.ObserveSpec;
import leshan.server.lwm2m.observation.ResourceObserver;
import leshan.server.lwm2m.tlv.Tlv;

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

	private void sleep(final long time) {
		try {
			Thread.sleep(time);
		} catch (final InterruptedException e) {
		}
	}

	@Test
	public void canObserveIntResourceWithAttribute() {
		register();

		sendCreate(new Tlv[0], INT_OBJECT_ID);

		WriteAttributesRequest.newRequest(getClient(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID,
				new ObserveSpec.Builder().greaterThan(6).build()).send(server.getRequestHandler());

		final ClientResponse response = sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID, observer);
		assertResponse(response, CONTENT, "0".getBytes());

		intResource.setValue(2);
		sleep(500);
		assertFalse(observer.receievedNotify().get());
	}

	public class Observer implements ResourceObserver {

		private byte[] content;
		private ContentFormat contentFormat;
		private ClientResourceSpec target;
		private final AtomicBoolean notified = new AtomicBoolean(false);

		@Override
		public void notify(final byte[] content, final ContentFormat contentFormat,
				final ClientResourceSpec target) {
			this.content = content;
			this.contentFormat = contentFormat;
			this.target = target;
			notified.set(true);
		}

		public AtomicBoolean receievedNotify() {
			return notified;
		}

		public byte[] getContent() {
			return content;
		}

		public ContentFormat getContentFormat() {
			return contentFormat;
		}

		public ClientResourceSpec getTarget() {
			return target;
		}

	}

}
