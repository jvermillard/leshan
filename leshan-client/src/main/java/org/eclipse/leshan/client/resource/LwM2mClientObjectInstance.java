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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ValueResponse;

public class LwM2mClientObjectInstance extends LwM2mClientNode {

    private final LwM2mClientObjectDefinition definition;
    private final Map<Integer, LwM2mClientResource> resources;
    private final int id;
    private final LwM2mClientObject parent;

    public LwM2mClientObjectInstance(final int id, final LwM2mClientObject parent,
            final LwM2mClientObjectDefinition definition) {
        this.id = id;
        this.resources = new HashMap<>();
        this.definition = definition;
        this.parent = parent;
    }

    public int getId() {
        return id;
    }

    public void createMandatory() {
        for (final LwM2mClientResourceDefinition def : definition.getResourceDefinitions()) {
            LwM2mClientResource resource = def.createResource();
            resource.setId(def.getId());
            resources.put(def.getId(), def.createResource());
        }
    }

    @Override
    public ValueResponse read() {
        List<LwM2mResource> resourcesRes = new ArrayList<LwM2mResource>();

        for (LwM2mClientResource resource : resources.values()) {
            ValueResponse response = resource.read();
            if (response.getCode() == ResponseCode.CONTENT) {
                LwM2mNode content = response.getContent();
                if (content instanceof LwM2mResource) {
                    resourcesRes.add((LwM2mResource) content);
                } else {
                    // TODO should rise an error
                    // return new ValueResponse(ResponseCode.)
                }
            }
        }

        return new ValueResponse(ResponseCode.CONTENT, new LwM2mObjectInstance(id,
                resourcesRes.toArray(new LwM2mResource[0])));
    }

    @Override
    public LwM2mResponse write(LwM2mNode node) {
        if (node instanceof LwM2mObjectInstance) {
            for (LwM2mResource resource : ((LwM2mObjectInstance) node).getResources().values()) {
                LwM2mResponse response = resources.get(resource.getId()).write(resource);
                if (response.getCode() != ResponseCode.CHANGED) {
                    return response;
                }
            }
        }
        return new LwM2mResponse(ResponseCode.CHANGED);
    }

    public void addResource(final Integer resourceId, final LwM2mClientResource resource) {
        resources.put(resourceId, resource);
    }

    public Map<Integer, LwM2mClientResource> getAllResources() {
        return new HashMap<>(resources);
    }

    public LwM2mResponse delete() {
        parent.delete(id);
        return new LwM2mResponse(ResponseCode.DELETED);
    }

}
