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

import static leshan.server.client.IntegrationTestHelper.GOOD_OBJECT_INSTANCE_ID;
import static leshan.server.client.IntegrationTestHelper.INT_OBJECT_ID;
import static leshan.server.client.IntegrationTestHelper.INT_RESOURCE_ID;
import static leshan.server.client.IntegrationTestHelper.assertResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import leshan.server.lwm2m.node.LwM2mNode;
import leshan.server.lwm2m.node.LwM2mObject;
import leshan.server.lwm2m.node.LwM2mObjectInstance;
import leshan.server.lwm2m.node.LwM2mResource;
import leshan.server.lwm2m.node.Value;
import leshan.server.lwm2m.observation.Observation;
import leshan.server.lwm2m.observation.ObservationRegistryListener;
import leshan.server.lwm2m.observation.ObserveSpec;
import leshan.server.lwm2m.request.CoapResponseCode.ResponseCode;
import leshan.server.lwm2m.request.ValueResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.awaitility.Awaitility;

public class ObserveTest {

    private SampleObservation observer;

    private IntegrationTestHelper helper = new IntegrationTestHelper();

    @After
    public void stop() {
        helper.stop();
    }

    @Before
    public void setupObservation() {
        observer = new SampleObservation();
        helper.observationRegistry.addListener(observer);

        helper.register();
        create();
    }

    @Test
    public void can_observe_resource() {
        observeResource();

        helper.intResource.setValue(2);
        assertObservedResource("2");
    }

    @Ignore
    @Test
    public void can_observe_resource_with_gt_with_notify() {
        observeResource(attributes().greaterThan(6));

        helper.intResource.setValue(20);
        assertObservedResource("20");
    }

    @Ignore
    @Test
    public void can_observe_resource_with_gt_no_notify() {
        observeResource(attributes().greaterThan(6));

        helper.intResource.setValue(2);
        assertNoObservation(500);
    }

    @Ignore
    @Test
    public void can_observe_resource_with_lt_with_notify() {
        observeResource(attributes().lessThan(6));

        helper.intResource.setValue(2);
        assertObservedResource("2");
    }

    @Ignore
    @Test
    public void can_observe_resource_with_lt_no_notify() {
        observeResource(attributes().lessThan(6));

        helper.intResource.setValue(20);
        assertNoObservation(500);
    }

    @Ignore
    @Test
    public void can_observe_resource_with_gt_and_lt_with_notify() {
        observeResource(attributes().greaterThan(10).lessThan(6));

        helper.intResource.setValue(20);
        assertObservedResource("20");
    }

    @Test
    public void can_observe_resource_with_pmax_with_notify() {
        observeResource(attributes().maxPeriod(1));

        assertObservedResource(2000, "0");
    }

    @Test
    public void can_observe_resource_with_pmax_no_notify() {
        observeResource(attributes().maxPeriod(1));

        assertNoObservation(500);
    }

    @Test
    public void can_observe_object_instance_with_pmax_with_notify() {
        observeObjectInstance(attributes().maxPeriod(1));

        helper.intResource.setValue(2);
        assertObservedObjectInstance(2000, "2");
    }

    @Test
    public void can_observe_object_with_pmax_with_notify() {
        observeObject(attributes().maxPeriod(1));

        helper.intResource.setValue(2);
        assertObservedObject(2000, "2");
    }

    private void create() {
        helper.sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), INT_OBJECT_ID);
    }

    private ObserveSpec.Builder attributes() {
        return new ObserveSpec.Builder();
    }

    private void observeResource() {
        final ValueResponse response = helper.sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
        assertResponse(response, ResponseCode.CONTENT, new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue("0")));
    }

    private void observeResource(final ObserveSpec.Builder observeSpecBuilder) {
        helper.sendWriteAttributes(observeSpecBuilder.build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INT_RESOURCE_ID);
        observeResource();
    }

    private void observeObjectInstance() {
        final ValueResponse response = helper.sendObserve(INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID);
        assertResponse(response, ResponseCode.CONTENT, new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID,
                new LwM2mResource[] { new LwM2mResource(INT_RESOURCE_ID, Value.newBinaryValue("0".getBytes())) }));
    }

    private void observeObjectInstance(final ObserveSpec.Builder observeSpecBuilder) {
        helper.sendWriteAttributes(observeSpecBuilder.build(), INT_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID);
        observeObjectInstance();
    }

    private void observeObject() {
        final ValueResponse response = helper.sendObserve(INT_OBJECT_ID);
        assertResponse(
                response,
                ResponseCode.CONTENT,
                new LwM2mObject(INT_OBJECT_ID, new LwM2mObjectInstance[] { new LwM2mObjectInstance(
                        GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] { new LwM2mResource(INT_RESOURCE_ID, Value
                                .newBinaryValue("0".getBytes())) }) }));
    }

    private void observeObject(final ObserveSpec.Builder observeSpecBuilder) {
        helper.sendWriteAttributes(observeSpecBuilder.build(), INT_OBJECT_ID);
        observeObject();
    }

    private void assertObservedResource(final String value) {
        assertObservedResource(500, value);
    }

    private void assertObservedResource(final long timeoutInSeconds, final String value) {
        Awaitility.await().atMost(timeoutInSeconds, TimeUnit.MILLISECONDS).untilTrue(observer.receievedNotify());
        assertEquals(new LwM2mResource(INT_RESOURCE_ID, Value.newStringValue(value)), observer.getContent());
    }

    private void assertObservedObjectInstance(final long timeoutInSeconds, final String resourceValue) {
        Awaitility.await().atMost(timeoutInSeconds, TimeUnit.MILLISECONDS).untilTrue(observer.receievedNotify());
        assertEquals(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] { new LwM2mResource(
                INT_RESOURCE_ID, Value.newBinaryValue(resourceValue.getBytes())) }), observer.getContent());
    }

    private void assertObservedObject(final long timeoutInSeconds, final String resourceValue) {
        Awaitility.await().atMost(timeoutInSeconds, TimeUnit.MILLISECONDS).untilTrue(observer.receievedNotify());
        assertEquals(
                new LwM2mObject(INT_OBJECT_ID, new LwM2mObjectInstance[] { new LwM2mObjectInstance(
                        GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] { new LwM2mResource(INT_RESOURCE_ID,
                                Value.newBinaryValue(resourceValue.getBytes())) }) }), observer.getContent());
    }

    private void assertNoObservation(final long time) {
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
        private final AtomicBoolean receivedNotify = new AtomicBoolean();
        private LwM2mNode content;

        @Override
        public void newValue(final Observation observation, final LwM2mNode value) {
            receivedNotify.set(true);
            content = value;
        }

        @Override
        public void cancelled(final Observation observation) {

        }

        @Override
        public void newObservation(final Observation observation) {

        }

        public AtomicBoolean receievedNotify() {
            return receivedNotify;
        }

        public LwM2mNode getContent() {
            return content;
        }
    }

}
