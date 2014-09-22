package leshan.server.client;

import static org.junit.Assert.assertEquals;
import leshan.server.lwm2m.node.LwM2mObjectInstance;
import leshan.server.lwm2m.node.LwM2mResource;
import leshan.server.lwm2m.node.Value;
import leshan.server.lwm2m.request.CreateResponse;
import leshan.server.lwm2m.request.ResponseCode;

import org.junit.Ignore;
import org.junit.Test;

public class CreateTest extends LwM2mClientServerIntegrationTest {

    @Test
    public void canCreateInstanceOfObject() {
        register();

        final CreateResponse response = sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);
        assertEmptyResponse(response, ResponseCode.CREATED);
        assertEquals(GOOD_OBJECT_ID + "/0", response.getLocation());
    }

    @Test
    public void canCreateSpecificInstanceOfObject() {
        register();

        final CreateResponse response = sendCreate(createGoodObjectInstance("one", "two"), GOOD_OBJECT_ID, 14);
        assertEmptyResponse(response, ResponseCode.CREATED);
        assertEquals(GOOD_OBJECT_ID + "/14", response.getLocation());
    }

    @Test
    public void canCreateMultipleInstanceOfObject() {
        register();

        final CreateResponse response = sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);
        assertEmptyResponse(response, ResponseCode.CREATED);
        assertEquals(GOOD_OBJECT_ID + "/0", response.getLocation());

        final CreateResponse responseTwo = sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);
        assertEmptyResponse(responseTwo, ResponseCode.CREATED);
        assertEquals(GOOD_OBJECT_ID + "/1", responseTwo.getLocation());
    }

    @Test
    public void cannotCreateInstanceOfObject() {
        register();

        final CreateResponse response = sendCreate(createGoodObjectInstance("hello", "goodbye"), BAD_OBJECT_ID);
        assertEmptyResponse(response, ResponseCode.NOT_FOUND);
    }

    @Test
    public void cannotCreateInstanceWithoutAllRequiredResources() {
        register();

        LwM2mObjectInstance instance = new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] {
                new LwM2mResource(FIRST_RESOURCE_ID, Value.newStringValue("hello"))
        });

        final CreateResponse response = sendCreate(instance, GOOD_OBJECT_ID);
        assertEmptyResponse(response, ResponseCode.BAD_REQUEST);

        assertEmptyResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.NOT_FOUND);
    }

    @Test
    public void cannotCreateInstanceWithExtraneousResources() {
        register();

        LwM2mObjectInstance instance = new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] {
                new LwM2mResource(FIRST_RESOURCE_ID, Value.newStringValue("hello")),
                new LwM2mResource(SECOND_RESOURCE_ID, Value.newStringValue("goodbye")),
                new LwM2mResource(INVALID_RESOURCE_ID, Value.newStringValue("lolz"))
        });

        final CreateResponse response = sendCreate(instance, GOOD_OBJECT_ID);
        assertEmptyResponse(response, ResponseCode.METHOD_NOT_ALLOWED);

        assertEmptyResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.NOT_FOUND);
    }

    @Test
    public void cannotCreateInstanceWithNonWritableResource() {
        register();

        LwM2mObjectInstance instance = new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] {
                new LwM2mResource(FIRST_RESOURCE_ID, Value.newStringValue("hello")),
                new LwM2mResource(SECOND_RESOURCE_ID, Value.newStringValue("goodbye")),
                new LwM2mResource(EXECUTABLE_RESOURCE_ID, Value.newStringValue("lolz"))
        });

        final CreateResponse response = sendCreate(instance, GOOD_OBJECT_ID);
        assertEmptyResponse(response, ResponseCode.METHOD_NOT_ALLOWED);

        assertEmptyResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.NOT_FOUND);
    }

    @Test
    public void canCreateObjectInstanceWithEmptyPayload() {
        register();
        assertEmptyResponse(sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), MULTIPLE_OBJECT_ID),
                ResponseCode.CREATED);
    }

    @Test
    public void cannotCreateMandatorySingleObject() {
        register();
        assertEmptyResponse(sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), MANDATORY_SINGLE_OBJECT_ID),
                ResponseCode.BAD_REQUEST);
    }

    @Test
    public void canCreateMandatoryMultipleObject() {
        register();
        assertEmptyResponse(sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), MANDATORY_MULTIPLE_OBJECT_ID),
                ResponseCode.CREATED);
        assertEmptyResponse(sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), MANDATORY_MULTIPLE_OBJECT_ID),
                ResponseCode.CREATED);
    }

    @Test
    public void cannotCreateMoreThanOneSingleObject() {
        register();
        assertEmptyResponse(sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), OPTIONAL_SINGLE_OBJECT_ID),
                ResponseCode.CREATED);
        assertEmptyResponse(sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), OPTIONAL_SINGLE_OBJECT_ID),
                ResponseCode.BAD_REQUEST);
    }

}
