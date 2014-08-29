package leshan.server.client;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.operation.Executable;
import leshan.client.lwm2m.operation.Readable;
import leshan.client.lwm2m.operation.Writable;
import leshan.client.lwm2m.resource.ClientObject;
import leshan.client.lwm2m.resource.SingleResourceDefinition;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.tlv.TlvEncoder;

import org.junit.Test;

public class ReadTest extends LwM2mClientServerIntegrationTest {

	@Override
	protected LwM2mClient createClient() {
		final ReadWriteListenerWithBrokenWrite brokenResourceListener = new ReadWriteListenerWithBrokenWrite();

		final ClientObject objectOne = new ClientObject(GOOD_OBJECT_ID,
				new SingleResourceDefinition(FIRST_RESOURCE_ID, firstReadableWritable, firstReadableWritable, Executable.NOT_EXECUTABLE),
				new SingleResourceDefinition(SECOND_RESOURCE_ID, secondReadableWritable, secondReadableWritable, Executable.NOT_EXECUTABLE),
				new SingleResourceDefinition(EXECUTABLE_RESOURCE_ID, Readable.NOT_READABLE, Writable.NOT_WRITABLE, executableAlwaysSuccessful));
		final ClientObject objectTwo = new ClientObject(BROKEN_OBJECT_ID,
				new SingleResourceDefinition(BROKEN_RESOURCE_ID, brokenResourceListener, brokenResourceListener, Executable.NOT_EXECUTABLE));
		return new LwM2mClient(objectOne, objectTwo);
	}

	@Test
	public void canReadObject() {
		register();
		assertResponse(sendRead(GOOD_OBJECT_ID), ResponseCode.CONTENT, new byte[0]);
	}

	@Test
	public void canReadObjectWithCreatedInstance() {
		register();
		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		assertResponse(sendRead(GOOD_OBJECT_ID), ResponseCode.CONTENT, TlvEncoder.encode(createObjectInstaceTlv("hello", "goodbye")).array());
	}

	@Test
	public void canReadObjectInstace() {
		register();
		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.CONTENT, TlvEncoder.encode(createResourcesTlv("hello", "goodbye")).array());
	}

	@Test
	public void canReadResource() {
		register();
		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);

		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID),
				ResponseCode.CONTENT, "hello".getBytes());
		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
				ResponseCode.CONTENT, "goodbye".getBytes());
	}

}
