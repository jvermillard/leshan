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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.Value;
import org.eclipse.leshan.core.objectspec.ObjectSpec;
import org.eclipse.leshan.core.objectspec.ResourceSpec;

public class SimpleInstanceEnabler implements LwM2mInstanceEnabler {

    Map<Integer, LwM2mResource> resources = new HashMap<Integer, LwM2mResource>();
    List<ResourceChangedListener> listeners = new ArrayList<ResourceChangedListener>();

    @Override
    public LwM2mResource read(int resourceid) {
        return resources.get(resourceid);
    }

    @Override
    public void write(int resourceid, LwM2mResource value) {
        resources.put(resourceid, value);
    }

    @Override
    public void execute(int resourceid, byte[] params) {
        System.out.println("Execute on resource " + resourceid + " params " + params);
    }

    @Override
    public void setObjectSpec(ObjectSpec objectSpec) {
        // initialize resources
        for (ResourceSpec resourceSpec : objectSpec.resources.values()) {
            if (resourceSpec.operations.isReadable()) {
                LwM2mResource newResource = createResource(objectSpec, resourceSpec);
                if (newResource != null) {
                    resources.put(newResource.getId(), newResource);
                }
            }
        }
    }

    protected LwM2mResource createResource(ObjectSpec objectSpec, ResourceSpec resourceSpec) {
        if (!resourceSpec.multiple) {
            Value<?> value;
            switch (resourceSpec.type) {
            case STRING:
                value = createDefaultStringValue(objectSpec, resourceSpec);
                break;
            case BOOLEAN:
                value = createDefaultBooleanValue(objectSpec, resourceSpec);
                break;
            case INTEGER:
                value = createDefaultIntegerValue(objectSpec, resourceSpec);
                break;
            case FLOAT:
                value = createDefaultFloatValue(objectSpec, resourceSpec);
                break;
            case TIME:
                value = createDefaultDateValue(objectSpec, resourceSpec);
                break;
            case OPAQUE:
                value = createDefaultOpaqueValue(objectSpec, resourceSpec);
                break;
            default:
                // this should not happened
                value = null;
                break;
            }
            if (value != null)
                return new LwM2mResource(resourceSpec.id, value);
        } else {
            Value<?>[] values;
            switch (resourceSpec.type) {
            case STRING:
                values = new Value[] { createDefaultStringValue(objectSpec, resourceSpec) };
                break;
            case BOOLEAN:
                values = new Value[] { createDefaultBooleanValue(objectSpec, resourceSpec) };
                break;
            case INTEGER:
                values = new Value[] { createDefaultIntegerValue(objectSpec, resourceSpec) };
                break;
            case FLOAT:
                values = new Value[] { createDefaultFloatValue(objectSpec, resourceSpec) };
                break;
            case TIME:
                values = new Value[] { createDefaultDateValue(objectSpec, resourceSpec) };
                break;
            case OPAQUE:
                values = new Value[] { createDefaultOpaqueValue(objectSpec, resourceSpec) };
                break;
            default:
                // this should not happened
                values = null;
                break;
            }
            if (values != null)
                return new LwM2mResource(resourceSpec.id, values);
        }
        return null;
    }

    protected Value<String> createDefaultStringValue(ObjectSpec objectSpec, ResourceSpec resourceSpec) {
        return Value.newStringValue("Default " + resourceSpec.name);
    }

    protected Value<Integer> createDefaultIntegerValue(ObjectSpec objectSpec, ResourceSpec resourceSpec) {
        return Value.newIntegerValue((int) (Math.random() * 100 % 101));
    }

    protected Value<Boolean> createDefaultBooleanValue(ObjectSpec objectSpec, ResourceSpec resourceSpec) {
        return Value.newBooleanValue(Math.random() * 100 % 2 == 0);
    }

    protected Value<?> createDefaultDateValue(ObjectSpec objectSpec, ResourceSpec resourceSpec) {
        return Value.newDateValue(new Date());
    }

    protected Value<?> createDefaultFloatValue(ObjectSpec objectSpec, ResourceSpec resourceSpec) {
        return Value.newFloatValue((float) Math.random() * 100);
    }

    protected Value<?> createDefaultOpaqueValue(ObjectSpec objectSpec, ResourceSpec resourceSpec) {
        return Value.newBinaryValue(new String("Default " + resourceSpec.name).getBytes());
    }

    @Override
    public void addResourceChangedListener(ResourceChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeResourceChangedListener(ResourceChangedListener listener) {
        listeners.remove(listener);
    }

    public void fireResourceChange(int resourceId) {
        for (ResourceChangedListener listener : listeners) {
            listener.resourceChanged(resourceId);
        }
    }
}
