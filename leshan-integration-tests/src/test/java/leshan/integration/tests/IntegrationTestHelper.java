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

package leshan.integration.tests;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import leshan.ObserveSpec;
import leshan.ResponseCode;
import leshan.bootstrap.BootstrapStoreImpl;
import leshan.client.LwM2mClient;
import leshan.client.californium.LeshanClient;
import leshan.client.exchange.LwM2mExchange;
import leshan.client.request.AbstractLwM2mClientRequest;
import leshan.client.request.BootstrapRequest;
import leshan.client.request.DeregisterRequest;
import leshan.client.request.LwM2mClientRequest;
import leshan.client.request.RegisterRequest;
import leshan.client.request.UpdateRequest;
import leshan.client.request.identifier.ClientIdentifier;
import leshan.client.resource.LwM2mClientObjectDefinition;
import leshan.client.resource.SingleResourceDefinition;
import leshan.client.resource.integer.IntegerLwM2mExchange;
import leshan.client.resource.integer.IntegerLwM2mResource;
import leshan.client.resource.multiple.MultipleLwM2mExchange;
import leshan.client.resource.multiple.MultipleLwM2mResource;
import leshan.client.resource.string.StringLwM2mExchange;
import leshan.client.resource.string.StringLwM2mResource;
import leshan.client.response.ExecuteResponse;
import leshan.client.response.OperationResponse;
import leshan.client.util.ResponseCallback;
import leshan.core.node.LwM2mNode;
import leshan.core.node.LwM2mObjectInstance;
import leshan.core.node.LwM2mResource;
import leshan.core.node.Value;
import leshan.core.request.ContentFormat;
import leshan.core.response.ClientResponse;
import leshan.core.response.CreateResponse;
import leshan.core.response.DiscoverResponse;
import leshan.core.response.ValueResponse;
import leshan.server.LwM2mServer;
import leshan.server.californium.LeshanServerBuilder;
import leshan.server.californium.impl.LwM2mBootstrapServerImpl;
import leshan.server.client.Client;
import leshan.server.client.ClientRegistry;
import leshan.server.impl.SecurityRegistryImpl;
import leshan.server.observation.ObservationRegistry;
import leshan.server.request.CreateRequest;
import leshan.server.request.DeleteRequest;
import leshan.server.request.DiscoverRequest;
import leshan.server.request.ObserveRequest;
import leshan.server.request.ReadRequest;
import leshan.server.request.WriteAttributesRequest;
import leshan.server.request.WriteRequest;

import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.LinkFormat;

/**
 * Helper for running a server and executing a client against it.
 * 
 */
public final class IntegrationTestHelper {

    static final int GOOD_OBJECT_ID = 100;
    static final int GOOD_OBJECT_INSTANCE_ID = 0;
    static final int FIRST_RESOURCE_ID = 4;
    static final int SECOND_RESOURCE_ID = 5;
    static final int EXECUTABLE_RESOURCE_ID = 6;
    static final int INVALID_RESOURCE_ID = 9;

    static final int BROKEN_OBJECT_ID = GOOD_OBJECT_ID + 1;
    static final int BROKEN_RESOURCE_ID = 7;

    static final int MULTIPLE_OBJECT_ID = GOOD_OBJECT_ID + 2;
    static final int MULTIPLE_RESOURCE_ID = 0;

    static final int INT_OBJECT_ID = GOOD_OBJECT_ID + 3;
    static final int INT_RESOURCE_ID = 0;

    static final int MANDATORY_MULTIPLE_OBJECT_ID = GOOD_OBJECT_ID + 4;
    static final int MANDATORY_MULTIPLE_RESOURCE_ID = 0;

    static final int MANDATORY_SINGLE_OBJECT_ID = GOOD_OBJECT_ID + 5;
    static final int MANDATORY_SINGLE_RESOURCE_ID = 0;

    static final int OPTIONAL_SINGLE_OBJECT_ID = GOOD_OBJECT_ID + 6;
    static final int OPTIONAL_SINGLE_RESOURCE_ID = 0;

    static final int BAD_OBJECT_ID = 1000;
    static final String ENDPOINT_IDENTIFIER = "kdfflwmtm";
    static final int CLIENT_PORT = 44022;
    static final int TIMEOUT_MS = 5000;
    private final String clientDataModel = "</lwm2m>;rt=\"oma.lwm2m\", </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";

    LwM2mServer server;
    private final ClientRegistry clientRegistry;

    Map<String, String> clientParameters = new HashMap<>();
    LwM2mClient client;
    ExecutableResource executableResource;
    ValueResource firstResource;
    ValueResource secondResource;
    MultipleResource multipleResource;
    IntValueResource intResource;
    ObservationRegistry observationRegistry;

    Set<WebLink> objectsAndInstances = LinkFormat.parse(clientDataModel);

    final InetSocketAddress serverAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 5683);
    final InetSocketAddress clientAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), CLIENT_PORT);
    private LwM2mBootstrapServerImpl bootstrapServer;

    public IntegrationTestHelper() {
        this(false);
    }

    public IntegrationTestHelper(final boolean startBootstrap) {
        final InetSocketAddress serverAddressSecure = new InetSocketAddress(InetAddress.getLoopbackAddress(), 5684);
        server = new LeshanServerBuilder().setlocalAddress(serverAddress).setlocalAddressSecure(serverAddressSecure)
                .build();
        clientRegistry = server.getClientRegistry();
        observationRegistry = server.getObservationRegistry();

        firstResource = new ValueResource();
        secondResource = new ValueResource();
        executableResource = new ExecutableResource();
        multipleResource = new MultipleResource();
        intResource = new IntValueResource();

        if (startBootstrap) {
            startBootstrapServer();
            client = createClient(serverAddressSecure);
        } else {
            server.start();
            client = createClient(serverAddress);
        }
    }

    private void startBootstrapServer() {
        bootstrapServer = new LwM2mBootstrapServerImpl(new BootstrapStoreImpl(), new SecurityRegistryImpl());
        bootstrapServer.start();
    }

    LwM2mClient createClient(final InetSocketAddress serverAddress) {
        final ReadWriteListenerWithBrokenWrite brokenResourceListener = new ReadWriteListenerWithBrokenWrite();

        final boolean single = true;
        final boolean mandatory = true;

        final LwM2mClientObjectDefinition objectOne = new LwM2mClientObjectDefinition(GOOD_OBJECT_ID, !mandatory,
                !single, new SingleResourceDefinition(FIRST_RESOURCE_ID, firstResource, mandatory),
                new SingleResourceDefinition(SECOND_RESOURCE_ID, secondResource, mandatory),
                new SingleResourceDefinition(EXECUTABLE_RESOURCE_ID, executableResource, !mandatory));
        final LwM2mClientObjectDefinition objectTwo = new LwM2mClientObjectDefinition(BROKEN_OBJECT_ID, !mandatory,
                !single, new SingleResourceDefinition(BROKEN_RESOURCE_ID, brokenResourceListener, mandatory));
        final LwM2mClientObjectDefinition objectThree = new LwM2mClientObjectDefinition(MULTIPLE_OBJECT_ID, !mandatory,
                !single, new SingleResourceDefinition(MULTIPLE_RESOURCE_ID, multipleResource, !mandatory));
        final LwM2mClientObjectDefinition objectFour = new LwM2mClientObjectDefinition(INT_OBJECT_ID, !mandatory,
                !single, new SingleResourceDefinition(INT_RESOURCE_ID, intResource, !mandatory));
        final LwM2mClientObjectDefinition mandatoryMultipleObject = new LwM2mClientObjectDefinition(
                MANDATORY_MULTIPLE_OBJECT_ID, mandatory, !single, new SingleResourceDefinition(
                        MANDATORY_MULTIPLE_RESOURCE_ID, intResource, !mandatory));
        final LwM2mClientObjectDefinition mandatorySingleObject = new LwM2mClientObjectDefinition(
                MANDATORY_SINGLE_OBJECT_ID, mandatory, single, new SingleResourceDefinition(
                        MANDATORY_SINGLE_RESOURCE_ID, intResource, mandatory));
        final LwM2mClientObjectDefinition optionalSingleObject = new LwM2mClientObjectDefinition(
                OPTIONAL_SINGLE_OBJECT_ID, !mandatory, single, new SingleResourceDefinition(
                        OPTIONAL_SINGLE_RESOURCE_ID, intResource, !mandatory));
        return new LeshanClient(clientAddress, serverAddress, objectOne, objectTwo, objectThree, objectFour,
                mandatoryMultipleObject, mandatorySingleObject, optionalSingleObject);
    }

    public void stop() {
        client.stop();
        server.stop();
        if (bootstrapServer != null) {
            bootstrapServer.stop();
        }
    }

    private LwM2mClientRequest createRegisterRequest() {
        final RegisterRequest registerRequest = new RegisterRequest(ENDPOINT_IDENTIFIER, clientParameters);

        return registerRequest;
    }

    private LwM2mClientRequest createDeregisterRequest(final ClientIdentifier clientIdentifier) {
        final DeregisterRequest deregisterRequest = new DeregisterRequest(clientIdentifier);

        return deregisterRequest;
    }

    private LwM2mClientRequest createBoostrapRequest() {
        return new BootstrapRequest(ENDPOINT_IDENTIFIER);
    }

    public OperationResponse bootstrap() {
        client.start();

        final LwM2mClientRequest boostrapRequest = createBoostrapRequest();
        final OperationResponse response = client.send(boostrapRequest);
        return response;
    }

    public OperationResponse register() {
        client.start();
        final LwM2mClientRequest registerRequest = createRegisterRequest();
        final OperationResponse response = client.send(registerRequest);
        return response;
    }

    public void register(final ResponseCallback callback) {
        client.start();
        final LwM2mClientRequest registerRequest = createRegisterRequest();
        client.send(registerRequest, callback);
    }

    public OperationResponse update(final ClientIdentifier clientIdentifier, final Map<String, String> updatedParameters) {
        final AbstractLwM2mClientRequest updaterRequest = new UpdateRequest(clientIdentifier, updatedParameters);

        return client.send(updaterRequest);
    }

    public void deregister(final ClientIdentifier clientIdentifier, final ResponseCallback callback) {
        final LwM2mClientRequest deregisterRequest = createDeregisterRequest(clientIdentifier);
        client.send(deregisterRequest, callback);
    }

    static LwM2mObjectInstance createGoodObjectInstance(final String value0, final String value1) {
        return new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] {
                                new LwM2mResource(FIRST_RESOURCE_ID, Value.newStringValue(value0)),
                                new LwM2mResource(SECOND_RESOURCE_ID, Value.newStringValue(value1)) });
    }

    ValueResponse sendRead(final int objectId) {
        return server.send(new ReadRequest(getClient(), objectId));
    }

    ValueResponse sendRead(final int objectId, final int objectInstanceId) {
        return server.send(new ReadRequest(getClient(), objectId, objectInstanceId));
    }

    ValueResponse sendRead(final int objectId, final int objectInstanceId, final int resourceId) {
        return server.send(new ReadRequest(getClient(), objectId, objectInstanceId, resourceId));
    }

    ValueResponse sendObserve(final int objectId) {
        return server.send(new ObserveRequest(getClient(), objectId));
    }

    ValueResponse sendObserve(final int objectId, final int objectInstanceId) {
        return server.send(new ObserveRequest(getClient(), objectId, objectInstanceId));
    }

    ValueResponse sendObserve(final int objectId, final int objectInstanceId, final int resourceId) {
        return server.send(new ObserveRequest(getClient(), objectId, objectInstanceId, resourceId));
    }

    DiscoverResponse sendDiscover(final int objectId) {
        return server.send(new DiscoverRequest(getClient(), objectId));
    }

    DiscoverResponse sendDiscover(final int objectId, final int objectInstanceId) {
        return server.send(new DiscoverRequest(getClient(), objectId, objectInstanceId));
    }

    DiscoverResponse sendDiscover(final int objectId, final int objectInstanceId, final int resourceId) {
        return server.send(new DiscoverRequest(getClient(), objectId, objectInstanceId, resourceId));
    }

    CreateResponse sendCreate(final LwM2mObjectInstance instance, final int objectId) {
        return server.send(new CreateRequest(getClient(), objectId, instance, ContentFormat.TLV));
    }

    CreateResponse sendCreate(final LwM2mObjectInstance instance, final int objectId, final int objectInstanceId) {
        return server.send(new CreateRequest(getClient(), objectId, objectInstanceId, instance, ContentFormat.TLV));
    }

    ClientResponse sendDelete(final int objectId, final int objectInstanceId) {
        return server.send(new DeleteRequest(getClient(), objectId, objectInstanceId));
    }

    ClientResponse sendUpdate(final LwM2mResource resource, final int objectId, final int objectInstanceId,
            final int resourceId) {
        final boolean isReplace = true;
        return server.send(new WriteRequest(getClient(), objectId, objectInstanceId, resourceId, resource,
                ContentFormat.TEXT, !isReplace));
    }

    ClientResponse sendUpdate(final String payload, final int objectId, final int objectInstanceId, final int resourceId) {
        final boolean isReplace = true;
        final LwM2mNode resource = new LwM2mResource(resourceId, Value.newStringValue(payload));
        return server.send(new WriteRequest(getClient(), objectId, objectInstanceId, resourceId, resource,
                ContentFormat.TEXT, !isReplace));
    }

    ClientResponse sendReplace(final LwM2mResource resource, final int objectId, final int objectInstanceId,
            final int resourceId) {
        final boolean isReplace = true;
        return server.send(new WriteRequest(getClient(), objectId, objectInstanceId, resourceId, resource,
                ContentFormat.TEXT, isReplace));
    }

    ClientResponse sendReplace(final String payload, final int objectId, final int objectInstanceId,
            final int resourceId) {
        final boolean isReplace = true;
        final LwM2mNode resource = new LwM2mResource(resourceId, Value.newStringValue(payload));
        return server.send(new WriteRequest(getClient(), objectId, objectInstanceId, resourceId, resource,
                ContentFormat.TEXT, isReplace));
    }

    ClientResponse sendWriteAttributes(final ObserveSpec observeSpec, final int objectId) {
        return server.send(new WriteAttributesRequest(getClient(), objectId, observeSpec));
    }

    ClientResponse sendWriteAttributes(final ObserveSpec observeSpec, final int objectId, final int objectInstanceId) {
        return server.send(new WriteAttributesRequest(getClient(), objectId, objectInstanceId, observeSpec));
    }

    ClientResponse sendWriteAttributes(final ObserveSpec observeSpec, final int objectId, final int objectInstanceId,
            final int resourceId) {
        return server
                .send(new WriteAttributesRequest(getClient(), objectId, objectInstanceId, resourceId, observeSpec));
    }

    Client getClient() {
        return clientRegistry.get(ENDPOINT_IDENTIFIER);
    }

    static void assertResponse(final ValueResponse response, final ResponseCode expectedCode,
            final LwM2mNode expectedContent) {
        assertEquals(expectedCode, response.getCode());
        assertEquals(expectedContent, response.getContent());
    }

    static void assertEmptyResponse(final ClientResponse response, final ResponseCode responseCode) {
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
