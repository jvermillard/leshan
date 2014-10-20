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
package leshan.client.lwm2m.resource;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import leshan.client.lwm2m.exchange.LwM2mCallbackExchange;
import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.exchange.aggregate.AggregatedLwM2mExchange;
import leshan.client.lwm2m.exchange.aggregate.LwM2mObjectReadResponseAggregator;
import leshan.client.lwm2m.exchange.aggregate.LwM2mResponseAggregator;
import leshan.client.lwm2m.response.CreateResponse;
import leshan.client.lwm2m.response.DeleteResponse;
import leshan.client.lwm2m.response.ReadResponse;
import leshan.client.lwm2m.response.WriteResponse;

public class LwM2mClientObject extends LwM2mClientNode {

    private final LwM2mClientObjectDefinition definition;
    private final AtomicInteger instanceCounter;
    private final Map<Integer, LwM2mClientObjectInstance> instances;

    public LwM2mClientObject(final LwM2mClientObjectDefinition definition) {
        this.definition = definition;
        this.instanceCounter = new AtomicInteger(0);
        this.instances = new ConcurrentHashMap<>();
    }

    public LwM2mClientObjectInstance createMandatoryInstance() {
        LwM2mClientObjectInstance instance = createNewInstance(false, 0);
        instance.createMandatory();
        return instance;
    }

    public void createInstance(final LwM2mCallbackExchange<LwM2mClientObjectInstance> exchange) {
        if (instanceCounter.get() >= 1 && definition.isSingle()) {
            exchange.respond(CreateResponse.invalidResource());
        }

        final LwM2mClientObjectInstance instance = createNewInstance(exchange.hasObjectInstanceId(),
                exchange.getObjectInstanceId());
        exchange.setNode(instance);
        instance.createInstance(exchange);
    }

    @Override
    public void read(LwM2mExchange exchange) {
        final Collection<LwM2mClientObjectInstance> instances = this.instances.values();

        if (instances.isEmpty()) {
            exchange.respond(ReadResponse.success(new byte[0]));
            return;
        }

        final LwM2mResponseAggregator aggr = new LwM2mObjectReadResponseAggregator(exchange, instances.size());
        for (final LwM2mClientObjectInstance inst : instances) {
            inst.read(new AggregatedLwM2mExchange(aggr, inst.getId()));
        }
    }

    @Override
    public void write(LwM2mExchange exchange) {
        exchange.respond(WriteResponse.notAllowed());
    }

    private LwM2mClientObjectInstance createNewInstance(boolean hasObjectInstanceId, int objectInstanceId) {
        final int newInstanceId = getNewInstanceId(hasObjectInstanceId, objectInstanceId);
        final LwM2mClientObjectInstance instance = new LwM2mClientObjectInstance(newInstanceId, this, definition);
        return instance;
    }

    public void onSuccessfulCreate(final LwM2mClientObjectInstance instance) {
        instances.put(instance.getId(), instance);
    }

    private int getNewInstanceId(boolean hasObjectInstanceId, int objectInstanceId) {
        if (hasObjectInstanceId) {
            return objectInstanceId;
        } else {
            return instanceCounter.getAndIncrement();
        }
    }

    public void delete(LwM2mExchange exchange, int id) {
        instances.remove(id);
        exchange.respond(DeleteResponse.success());
    }

}