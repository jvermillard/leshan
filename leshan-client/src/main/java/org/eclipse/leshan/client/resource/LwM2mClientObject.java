/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Zebra Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.client.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mObject;
import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.response.CreateResponse;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ValueResponse;

public class LwM2mClientObject extends LwM2mClientNode {

    private final LwM2mClientObjectDefinition definition;
    private final Map<Integer, LwM2mClientObjectInstance> instances;

    public LwM2mClientObject(final LwM2mClientObjectDefinition definition) {
        this.definition = definition;
        this.instances = new ConcurrentHashMap<>();
    }

    public LwM2mClientObjectInstance createMandatoryInstance() {
        LwM2mClientObjectInstance instance = new LwM2mClientObjectInstance(0, this, definition);
        instance.createMandatory();
        return instance;
    }

    public CreateResponse createInstance(LwM2mNode node) {
        if (instances.size() >= 1 && definition.isSingle()) {
            return new CreateResponse(ResponseCode.BAD_REQUEST);
        }
        if (!(node instanceof LwM2mObjectInstance))
            return new CreateResponse(ResponseCode.BAD_REQUEST);

        LwM2mObjectInstance instanceNode = (LwM2mObjectInstance) node;

        final LwM2mClientObjectInstance instance = new LwM2mClientObjectInstance(instanceNode.getId(), this, definition);
        instance.createMandatory();
        LwM2mResponse response = instance.write(node);
        if (response.getCode() == ResponseCode.CHANGED) {
            this.instances.put(instance.getId(), instance);
            return new CreateResponse(ResponseCode.CREATED);
        } else
            return new CreateResponse(response.getCode());
    }

    @Override
    public ValueResponse read() {
        List<LwM2mObjectInstance> instancesRes = new ArrayList<LwM2mObjectInstance>();

        for (LwM2mClientObjectInstance resource : instances.values()) {
            ValueResponse response = resource.read();
            if (response.getCode() == ResponseCode.CONTENT) {
                LwM2mNode content = response.getContent();
                if (content instanceof LwM2mObjectInstance) {
                    instancesRes.add((LwM2mObjectInstance) content);
                } else {
                    // TODO should rise an error
                    // return new ValueResponse(ResponseCode.)
                }
            }
        }

        return new ValueResponse(ResponseCode.CONTENT, new LwM2mObject(definition.getId(),
                instancesRes.toArray(new LwM2mObjectInstance[0])));
    }

    @Override
    public LwM2mResponse write(LwM2mNode node) {
        return new LwM2mResponse(ResponseCode.METHOD_NOT_ALLOWED);
    }

    public LwM2mResponse delete(int id) {
        instances.remove(id);
        return new LwM2mResponse(ResponseCode.DELETED);
    }

    public LwM2mClientObjectInstance getInstance(int id) {
        return instances.get(id);
    }
}