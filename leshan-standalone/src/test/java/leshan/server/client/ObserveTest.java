package leshan.server.client;

import java.util.concurrent.atomic.AtomicBoolean;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.operation.Executable;
import leshan.client.lwm2m.operation.Readable;
import leshan.client.lwm2m.operation.Writable;
import leshan.client.lwm2m.resource.ClientObject;
import leshan.client.lwm2m.resource.SingleResourceDefinition;
import leshan.server.lwm2m.message.ClientResourceSpec;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.observation.ResourceObserver;
import leshan.server.lwm2m.tlv.TlvEncoder;

import org.junit.Test;

import com.jayway.awaitility.Awaitility;

public class ObserveTest extends LwM2mClientServerIntegrationTest {

	private final Observer observer = new Observer();

	@Override
	protected LwM2mClient createClient() {
		final ClientObject objectOne = new ClientObject(GOOD_OBJECT_ID,
				new SingleResourceDefinition(FIRST_RESOURCE_ID, firstReadableWritable, firstReadableWritable, Executable.NOT_EXECUTABLE),
				new SingleResourceDefinition(SECOND_RESOURCE_ID, secondReadableWritable, secondReadableWritable, Executable.NOT_EXECUTABLE),
				new SingleResourceDefinition(EXECUTABLE_RESOURCE_ID, Readable.NOT_READABLE, Writable.NOT_WRITABLE, executableAlwaysSuccessful));
		return new LwM2mClient(objectOne);
	}

	@Test
	public void canObserveResource() {
		register();
		
		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);
		
		final ClientResponse response = sendObserve(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID, observer);
		assertResponse(response, ResponseCode.CONTENT, "hello".getBytes());
		
		firstReadableWritable.setValue("world");
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
