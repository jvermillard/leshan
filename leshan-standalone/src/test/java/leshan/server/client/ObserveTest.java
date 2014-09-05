package leshan.server.client;

import java.util.concurrent.atomic.AtomicBoolean;

import leshan.server.lwm2m.message.ClientResourceSpec;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.observation.ResourceObserver;

import org.junit.Test;

import com.jayway.awaitility.Awaitility;

public class ObserveTest extends LwM2mClientServerIntegrationTest {

	private final Observer observer = new Observer();

	@Test
	public void canObserveResource() {
		register();

		sendCreate(createGoodResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		final ClientResponse response = sendObserve(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID, observer);
		assertResponse(response, ResponseCode.CONTENT, "hello".getBytes());

		firstResource.setValue("world");
		Awaitility.await().untilTrue(observer.receievedNotify());
	}

	public class Observer implements ResourceObserver{

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
