package leshan.server.client;

import org.junit.Test;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.resource.ClientObject;
import leshan.client.lwm2m.resource.ExecuteListener;
import leshan.client.lwm2m.resource.ReadListener;
import leshan.client.lwm2m.resource.SingleResourceDefinition;
import leshan.client.lwm2m.resource.WriteListener;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.tlv.TlvEncoder;

public class DeleteTest extends LwM2mClientServerIntegrationTest {

	@Override
	protected LwM2mClient createClient() {
		final ReadWriteListenerWithBrokenWrite brokenResourceListener = new ReadWriteListenerWithBrokenWrite();

		final ClientObject objectOne = new ClientObject(GOOD_OBJECT_ID,
				new SingleResourceDefinition(FIRST_RESOURCE_ID, ExecuteListener.DUMMY, firstResourceListener, firstResourceListener),
				new SingleResourceDefinition(SECOND_RESOURCE_ID, ExecuteListener.DUMMY, secondResourceListener, secondResourceListener),
				new SingleResourceDefinition(EXECUTABLE_RESOURCE_ID, executeListener, WriteListener.DUMMY, ReadListener.DUMMY));
		final ClientObject objectTwo = new ClientObject(BROKEN_OBJECT_ID,
				new SingleResourceDefinition(BROKEN_RESOURCE_ID, ExecuteListener.DUMMY, brokenResourceListener, brokenResourceListener));
		return new LwM2mClient(objectOne, objectTwo);
	}
	
	@Test
	public void deleteCreatedObjectInstance(){
		register();
		
		createAndThenAssertDeleted();
	}
	
	@Test
	public void deleteAndCantReadObjectInstance(){
		register();
		
		createAndThenAssertDeleted();
		
		assertEmptyResponse(sendGet(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.NOT_FOUND);
	}

	private void createAndThenAssertDeleted() {
		sendCreate(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID);
		
		final ClientResponse responseDelete = sendDelete(createResourcesTlv("hello", "goodbye"), GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID);
		assertResponse(responseDelete, ResponseCode.DELETED, new byte[0]);
	}

}
