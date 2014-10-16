/*
 * Copyright (c) 2013, Sierra Wireless,
 * Copyright (c) 2014, Zebra Technologies,
 * 
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of {{ project }} nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package leshan.server.client;

import static org.junit.Assert.assertEquals;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.resource.LwM2mClientObjectDefinition;
import leshan.client.lwm2m.resource.SingleResourceDefinition;
import leshan.client.lwm2m.resource.integer.IntegerLwM2mExchange;
import leshan.client.lwm2m.resource.integer.IntegerLwM2mResource;
import leshan.client.lwm2m.resource.multiple.MultipleLwM2mExchange;
import leshan.client.lwm2m.resource.multiple.MultipleLwM2mResource;
import leshan.client.lwm2m.resource.string.StringLwM2mExchange;
import leshan.client.lwm2m.resource.string.StringLwM2mResource;
import leshan.client.lwm2m.response.ExecuteResponse;
import leshan.server.lwm2m.LeshanServer;
import leshan.server.lwm2m.LwM2mServer;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.impl.ClientRegistryImpl;
import leshan.server.lwm2m.impl.ObservationRegistryImpl;
import leshan.server.lwm2m.impl.security.SecurityRegistryImpl;
import leshan.server.lwm2m.node.LwM2mNode;
import leshan.server.lwm2m.node.LwM2mObjectInstance;
import leshan.server.lwm2m.node.LwM2mResource;
import leshan.server.lwm2m.node.Value;
import leshan.server.lwm2m.observation.ObservationRegistry;
import leshan.server.lwm2m.observation.ObserveSpec;
import leshan.server.lwm2m.request.ClientResponse;
import leshan.server.lwm2m.request.ContentFormat;
import leshan.server.lwm2m.request.CreateRequest;
import leshan.server.lwm2m.request.CreateResponse;
import leshan.server.lwm2m.request.DeleteRequest;
import leshan.server.lwm2m.request.DiscoverRequest;
import leshan.server.lwm2m.request.DiscoverResponse;
import leshan.server.lwm2m.request.ObserveRequest;
import leshan.server.lwm2m.request.ReadRequest;
import leshan.server.lwm2m.request.ResponseCode;
import leshan.server.lwm2m.request.ValueResponse;
import leshan.server.lwm2m.request.WriteAttributesRequest;
import leshan.server.lwm2m.request.WriteRequest;
import leshan.server.lwm2m.security.SecurityRegistry;

import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.LinkFormat;
import org.junit.After;
import org.junit.Before;

public abstract class LwM2mClientServerIntegrationTest {

    protected static final int GOOD_OBJECT_ID = 100;
    protected static final int GOOD_OBJECT_INSTANCE_ID = 0;
    protected static final int FIRST_RESOURCE_ID = 4;
    protected static final int SECOND_RESOURCE_ID = 5;
    protected static final int EXECUTABLE_RESOURCE_ID = 6;
    protected static final int INVALID_RESOURCE_ID = 9;

    protected static final int BROKEN_OBJECT_ID = GOOD_OBJECT_ID + 1;
    protected static final int BROKEN_RESOURCE_ID = 7;

    protected static final int MULTIPLE_OBJECT_ID = GOOD_OBJECT_ID + 2;
    protected static final int MULTIPLE_RESOURCE_ID = 0;

    protected static final int INT_OBJECT_ID = GOOD_OBJECT_ID + 3;
    protected static final int INT_RESOURCE_ID = 0;

    protected static final int MANDATORY_MULTIPLE_OBJECT_ID = GOOD_OBJECT_ID + 4;
    protected static final int MANDATORY_MULTIPLE_RESOURCE_ID = 0;

    protected static final int MANDATORY_SINGLE_OBJECT_ID = GOOD_OBJECT_ID + 5;
    protected static final int MANDATORY_SINGLE_RESOURCE_ID = 0;

    protected static final int OPTIONAL_SINGLE_OBJECT_ID = GOOD_OBJECT_ID + 6;
    protected static final int OPTIONAL_SINGLE_RESOURCE_ID = 0;

    protected static final int BAD_OBJECT_ID = 1000;
    protected static final String ENDPOINT = "epflwmtm";
    private static final int CLIENT_PORT = 44022;
    protected static final int TIMEOUT_MS = 5000;
    private final String clientDataModel = "</lwm2m>;rt=\"oma.lwm2m\", </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";

    protected LwM2mServer server;
    private ClientRegistryImpl clientRegistry;

    protected Map<String,String> clientParameters;

    protected Set<WebLink> objectsAndInstances;
    private InetSocketAddress serverAddress;
    protected LwM2mClient client;
    protected ExecutableResource executableResource;
    protected ValueResource firstResource;
    protected ValueResource secondResource;
    protected MultipleResource multipleResource;
    protected IntValueResource intResource;
    protected ObservationRegistry observationRegistry;

    @Before
    public void setup() {
        clientParameters = new HashMap<>();
        objectsAndInstances = LinkFormat.parse(clientDataModel);

        serverAddress = new InetSocketAddress(5683);
        final InetSocketAddress serverAddressSecure = new InetSocketAddress(5684);
        clientRegistry = new ClientRegistryImpl();
        observationRegistry = new ObservationRegistryImpl();
        final SecurityRegistry securityRegistry = new SecurityRegistryImpl();
        server = new LeshanServer(serverAddress, serverAddressSecure, clientRegistry, securityRegistry, observationRegistry);
        server.start();

        firstResource = new ValueResource();
        secondResource = new ValueResource();
        executableResource = new ExecutableResource();
        multipleResource = new MultipleResource();
        intResource = new IntValueResource();

        client = createClient();
    }

    protected LwM2mClient createClient() {
        final ReadWriteListenerWithBrokenWrite brokenResourceListener = new ReadWriteListenerWithBrokenWrite();

        final boolean single = true;
        final boolean mandatory = true;

        final LwM2mClientObjectDefinition objectOne = new LwM2mClientObjectDefinition(GOOD_OBJECT_ID, !mandatory, !single,
                new SingleResourceDefinition(FIRST_RESOURCE_ID, firstResource, mandatory),
                new SingleResourceDefinition(SECOND_RESOURCE_ID, secondResource, mandatory),
                new SingleResourceDefinition(EXECUTABLE_RESOURCE_ID, executableResource, !mandatory));
        final LwM2mClientObjectDefinition objectTwo = new LwM2mClientObjectDefinition(BROKEN_OBJECT_ID, !mandatory, !single,
                new SingleResourceDefinition(BROKEN_RESOURCE_ID, brokenResourceListener, mandatory));
        final LwM2mClientObjectDefinition objectThree = new LwM2mClientObjectDefinition(MULTIPLE_OBJECT_ID, !mandatory, !single,
                new SingleResourceDefinition(MULTIPLE_RESOURCE_ID, multipleResource, !mandatory));
        final LwM2mClientObjectDefinition objectFour = new LwM2mClientObjectDefinition(INT_OBJECT_ID, !mandatory, !single,
                new SingleResourceDefinition(INT_RESOURCE_ID, intResource, !mandatory));
        final LwM2mClientObjectDefinition mandatoryMultipleObject = new LwM2mClientObjectDefinition(MANDATORY_MULTIPLE_OBJECT_ID, mandatory, !single,
                new SingleResourceDefinition(MANDATORY_MULTIPLE_RESOURCE_ID, intResource, !mandatory));
        final LwM2mClientObjectDefinition mandatorySingleObject = new LwM2mClientObjectDefinition(MANDATORY_SINGLE_OBJECT_ID, mandatory, single,
                new SingleResourceDefinition(MANDATORY_SINGLE_RESOURCE_ID, intResource, mandatory));
        final LwM2mClientObjectDefinition optionalSingleObject = new LwM2mClientObjectDefinition(OPTIONAL_SINGLE_OBJECT_ID, !mandatory, single,
                new SingleResourceDefinition(OPTIONAL_SINGLE_RESOURCE_ID, intResource, !mandatory));
        return new LwM2mClient(objectOne, objectTwo, objectThree, objectFour, mandatoryMultipleObject, mandatorySingleObject, optionalSingleObject);
    }

    @After
    public void teardown() {
        client.stop();
        server.stop();
    }

    protected RegisterUplink registerAndGetUplink() {
        final RegisterUplink registerUplink = client.startRegistration(CLIENT_PORT, serverAddress);
        return registerUplink;
    }

    protected void register() {
        final RegisterUplink registerUplink = registerAndGetUplink();
        registerUplink.register(ENDPOINT, clientParameters, TIMEOUT_MS);
    }

    protected static LwM2mObjectInstance createGoodObjectInstance(final String value0, final String value1) {
        return new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] {
                new LwM2mResource(FIRST_RESOURCE_ID, Value.newStringValue(value0)),
                new LwM2mResource(SECOND_RESOURCE_ID, Value.newStringValue(value1))
        });
    }

    protected ValueResponse sendRead(final int objectId) {
        return server.send(new ReadRequest(getClient(), objectId));
    }

    protected ValueResponse sendRead(final int objectId, final int objectInstanceId) {
        return server.send(new ReadRequest(getClient(), objectId, objectInstanceId));
    }

    protected ValueResponse sendRead(final int objectId, final int objectInstanceId, final int resourceId) {
        return server.send(new ReadRequest(getClient(), objectId, objectInstanceId, resourceId));
    }

    protected ValueResponse sendObserve(final int objectId) {
        return server.send(new ObserveRequest(getClient(), objectId));
    }

    protected ValueResponse sendObserve(final int objectId, final int objectInstanceId) {
        return server.send(new ObserveRequest(getClient(), objectId, objectInstanceId));
    }

    protected ValueResponse sendObserve(final int objectId, final int objectInstanceId, final int resourceId) {
        return server.send(new ObserveRequest(getClient(), objectId, objectInstanceId, resourceId));
    }

    protected DiscoverResponse sendDiscover(final int objectId) {
        return server.send(new DiscoverRequest(getClient(), objectId));
    }		

    protected DiscoverResponse sendDiscover(final int objectId, final int objectInstanceId) {
        return server.send(new DiscoverRequest(getClient(), objectId, objectInstanceId));
    }

    protected DiscoverResponse sendDiscover(final int objectId, final int objectInstanceId, final int resourceId) {
        return server.send(new DiscoverRequest(getClient(), objectId, objectInstanceId, resourceId));
    }

    protected CreateResponse sendCreate(final LwM2mObjectInstance instance, final int objectId) {
        return server.send(new CreateRequest(getClient(), objectId, instance, ContentFormat.TLV));
    }

    protected CreateResponse sendCreate(final LwM2mObjectInstance instance, final int objectId, final int objectInstanceId) {
        return server.send(new CreateRequest(getClient(), objectId, objectInstanceId, instance, ContentFormat.TLV));
    }

    protected ClientResponse sendDelete(final int objectId, final int objectInstanceId) {
        return server.send(new DeleteRequest(getClient(), objectId, objectInstanceId));
    }

    protected ClientResponse sendUpdate(final LwM2mResource resource, final int objectId, final int objectInstanceId, final int resourceId) {
        final boolean isReplace = true;
        return server.send(new WriteRequest(getClient(), objectId, objectInstanceId, resourceId, resource, ContentFormat.TEXT, !isReplace));
    }

    protected ClientResponse sendUpdate(final String payload, final int objectId, final int objectInstanceId, final int resourceId) {
        final boolean isReplace = true;
        final LwM2mNode resource = new LwM2mResource(resourceId, Value.newStringValue(payload));
        return server.send(new WriteRequest(getClient(), objectId, objectInstanceId, resourceId, resource, ContentFormat.TEXT, !isReplace));
    }

    protected ClientResponse sendReplace(final LwM2mResource resource, final int objectId, final int objectInstanceId, final int resourceId) {
        final boolean isReplace = true;
        return server.send(new WriteRequest(getClient(), objectId, objectInstanceId, resourceId, resource, ContentFormat.TEXT, isReplace));
    }

    protected ClientResponse sendReplace(final String payload, final int objectId, final int objectInstanceId, final int resourceId) {
        final boolean isReplace = true;
        final LwM2mNode resource = new LwM2mResource(resourceId, Value.newStringValue(payload));
        return server.send(new WriteRequest(getClient(), objectId, objectInstanceId, resourceId, resource, ContentFormat.TEXT, isReplace));
    }

    protected ClientResponse sendWriteAttributes(final ObserveSpec observeSpec, final int objectId) {
        return server.send(new WriteAttributesRequest(getClient(), objectId, observeSpec));
    }

    protected ClientResponse sendWriteAttributes(final ObserveSpec observeSpec, final int objectId, final int objectInstanceId) {
        return server.send(new WriteAttributesRequest(getClient(), objectId, objectInstanceId, observeSpec));
    }

    protected ClientResponse sendWriteAttributes(final ObserveSpec observeSpec, final int objectId, final int objectInstanceId, final int resourceId) {
        return server.send(new WriteAttributesRequest(getClient(), objectId, objectInstanceId, resourceId, observeSpec));
    }

    protected Client getClient() {
        return clientRegistry.get(ENDPOINT);
    }

    protected void assertResponse(final ValueResponse response, final ResponseCode expectedCode, final LwM2mNode expectedContent) {
        assertEquals(expectedCode, response.getCode());
        assertEquals(expectedContent, response.getContent());
    }

    protected void assertEmptyResponse(final ClientResponse response, final ResponseCode responseCode) {
        assertEquals(responseCode, response.getCode());
    }

    public class ValueResource extends StringLwM2mResource {

        private String value = "blergs";

        public void setValue(final String newValue) {
            value = newValue;
            notifyResourceUpdated();
        }

        public String getValue() {
            return value;
        }

        @Override
        public void handleWrite(final StringLwM2mExchange exchange) {
            setValue(exchange.getRequestPayload());

            exchange.respondSuccess();
        }

        @Override
        public void handleRead(final StringLwM2mExchange exchange) {
            exchange.respondContent(value);
        }

    }

    public class IntValueResource extends IntegerLwM2mResource {

        private int value = 0;

        public void setValue(final int newValue) {
            value = newValue;
            notifyResourceUpdated();
        }

        public int getValue() {
            return value;
        }

        @Override
        public void handleWrite(final IntegerLwM2mExchange exchange) {
            setValue(exchange.getRequestPayload());

            exchange.respondSuccess();
        }

        @Override
        public void handleRead(final IntegerLwM2mExchange exchange) {
            exchange.respondContent(value);
        }

    }

    public class ReadWriteListenerWithBrokenWrite extends StringLwM2mResource {

        private String value;

        @Override
        public void handleWrite(final StringLwM2mExchange exchange) {
            if (value == null) {
                value = exchange.getRequestPayload();
                exchange.respondSuccess();
            } else {
                exchange.respondFailure();
            }
        }

        @Override
        public void handleRead(final StringLwM2mExchange exchange) {
            exchange.respondContent(value);
        }

    }

    public class ExecutableResource extends StringLwM2mResource {

        @Override
        public void handleExecute(final LwM2mExchange exchange) {
            exchange.respond(ExecuteResponse.success());
        }

    }

    public class MultipleResource extends MultipleLwM2mResource {

        private Map<Integer, byte[]> value;

        public void setValue(final Map<Integer, byte[]> initialValue) {
            this.value = initialValue;
        }

        @Override
        public void handleRead(final MultipleLwM2mExchange exchange) {
            exchange.respondContent(value);
        }

        @Override
        public void handleWrite(final MultipleLwM2mExchange exchange) {
            this.value = exchange.getRequestPayload();
            exchange.respondSuccess();
        }

    }

}
