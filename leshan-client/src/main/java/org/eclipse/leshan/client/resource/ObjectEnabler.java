/*******************************************************************************
 * Copyright (c) 2015 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.client.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.objectspec.ResourceSpec;
import org.eclipse.leshan.core.objectspec.Resources;
import org.eclipse.leshan.core.request.CreateRequest;
import org.eclipse.leshan.core.request.DeleteRequest;
import org.eclipse.leshan.core.request.ExecuteRequest;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.response.CreateResponse;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ValueResponse;

public class ObjectEnabler extends BaseObjectEnabler {

    // TODO we should manage that in a threadsafe way
    private Map<Integer, LwM2mInstanceEnabler> instances;
    private Class<? extends LwM2mInstanceEnabler> instanceClass;

    public ObjectEnabler(int id, Map<Integer, LwM2mInstanceEnabler> instances, Class<? extends LwM2mInstanceEnabler> instanceClass) {
        super(id);
        this.instances = new HashMap<Integer, LwM2mInstanceEnabler>(instances);
        this.instanceClass = instanceClass;
        for (Entry<Integer, LwM2mInstanceEnabler> entry : this.instances.entrySet()) {
            listenInstance(entry.getValue(), entry.getKey());
        }
    }

    @Override
    protected CreateResponse doCreate(CreateRequest request) {
        try {
            // TODO manage case where instanceid is not available
            LwM2mInstanceEnabler newInstance = instanceClass.newInstance();
            newInstance.setObjectSpec(Resources.getObjectSpec(getId()));

            LwM2mObjectInstance objectInstance = (LwM2mObjectInstance) request.getObjectInstance();
            for (LwM2mResource resource : objectInstance.getResources().values()) {
                newInstance.write(resource.getId(), resource);
            }
            instances.put(request.getPath().getObjectInstanceId(), newInstance);
            listenInstance(newInstance, request.getPath().getObjectInstanceId());

            return new CreateResponse(ResponseCode.CREATED, request.getPath().toString());
        } catch (InstantiationException | IllegalAccessException e) {
            // TODO not really a bad request ...
            return new CreateResponse(ResponseCode.BAD_REQUEST);
        }
    }

    @Override
    protected ValueResponse doRead(ReadRequest request) {
        LwM2mPath path = request.getPath();

        // Manage Object case
        if (path.isObject()) {
            // TODO
            return new ValueResponse(ResponseCode.CONTENT);
        }

        // Manage Instance case
        LwM2mInstanceEnabler instance = instances.get(path.getObjectInstanceId());
        if (instance == null)
            return new ValueResponse(ResponseCode.NOT_FOUND);

        if (path.getResourceId() == null) {
            List<LwM2mResource> resources = new ArrayList<>();
            for (ResourceSpec resourceSpec : Resources.getObjectSpec(getId()).resources.values()) {
                if (resourceSpec.operations.isReadable()) {
                    LwM2mResource resource = instance.read(resourceSpec.id);
                    if (resource != null)
                        resources.add(resource);
                }
            }
            return new ValueResponse(ResponseCode.CONTENT, new LwM2mObjectInstance(path.getObjectInstanceId(),
                    resources.toArray(new LwM2mResource[0])));
        }

        // Manage Resource case
        LwM2mResource resource = instance.read(path.getResourceId());
        if (resource != null)
            return new ValueResponse(ResponseCode.CONTENT, resource);
        else
            return new ValueResponse(ResponseCode.NOT_FOUND);
    }

    @Override
    protected LwM2mResponse doWrite(WriteRequest request) {
        LwM2mPath path = request.getPath();

        // Manage Instance case
        LwM2mInstanceEnabler instance = instances.get(path.getObjectInstanceId());
        if (instance == null)
            return new LwM2mResponse(ResponseCode.NOT_FOUND);

        if (path.getResourceId() == null) {
            for (LwM2mResource resource : ((LwM2mObjectInstance) request.getNode()).getResources().values()) {
                instance.write(resource.getId(), resource);
            }
            return new LwM2mResponse(ResponseCode.CHANGED);
        }

        // Manage Resource case
        instance.write(path.getResourceId(), (LwM2mResource) request.getNode());
        return new LwM2mResponse(ResponseCode.CHANGED);
    }

    @Override
    protected LwM2mResponse doExecute(ExecuteRequest request) {
        LwM2mPath path = request.getPath();
        LwM2mInstanceEnabler instance = instances.get(path.getObjectInstanceId());
        if (instance == null) {
            return new LwM2mResponse(ResponseCode.NOT_FOUND);
        }
        instance.execute(path.getResourceId(), request.getParameters());
        return new LwM2mResponse(ResponseCode.CHANGED);
    }

    @Override
    protected LwM2mResponse doDelete(DeleteRequest request) {
        LwM2mPath path = request.getPath();
        if (!instances.containsKey(path.getObjectInstanceId())) {
            return new LwM2mResponse(ResponseCode.NOT_FOUND);
        }
        instances.remove(request.getPath().getObjectInstanceId());
        return new LwM2mResponse(ResponseCode.DELETED);
    }

    private void listenInstance(LwM2mInstanceEnabler instance, final int instanceId) {
        instance.addResourceChangedListener(new ResourceChangedListener() {
            @Override
            public void resourceChanged(int resourceId) {
                getNotifySender().sendNotify(getId() + "/" + instanceId + "/" + resourceId);
            }
        });
    }

}
