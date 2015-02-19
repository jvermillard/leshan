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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.leshan.core.objectspec.ObjectSpec;
import org.eclipse.leshan.core.objectspec.Resources;

public class ObjectsInitializer {

    Map<Integer, Class<? extends LwM2mInstanceEnabler>> classes = new HashMap<Integer, Class<? extends LwM2mInstanceEnabler>>();

    public void setClassForObject(int objectId, Class<? extends LwM2mInstanceEnabler> clazz) {
        classes.put(objectId, clazz);
    }

    public List<ObjectEnabler> createMandatory() {
        Resources.load();

        Collection<ObjectSpec> objectSpecs = Resources.getObjectSpecs();

        List<ObjectEnabler> enablers = new ArrayList<ObjectEnabler>();
        for (ObjectSpec objectSpec : objectSpecs) {
            if (objectSpec.mandatory) {
                ObjectEnabler objectEnabler = createNodeEnabler(objectSpec);
                if (objectEnabler != null)
                    enablers.add(objectEnabler);
            }
        }
        return enablers;
    }

    public List<ObjectEnabler> create(int... objectId) {
        Resources.load();

        List<ObjectEnabler> enablers = new ArrayList<ObjectEnabler>();
        for (int i = 0; i < objectId.length; i++) {
            ObjectSpec objectSpec = Resources.getObjectSpec(objectId[i]);
            ObjectEnabler objectEnabler = createNodeEnabler(objectSpec);
            if (objectEnabler != null)
                enablers.add(objectEnabler);

        }
        return enablers;
    }

    protected ObjectEnabler createNodeEnabler(ObjectSpec objectSpec) {
        HashMap<Integer, LwM2mInstanceEnabler> instances = new HashMap<Integer, LwM2mInstanceEnabler>();

        if (!objectSpec.multiple) {
            LwM2mInstanceEnabler newInstance = createInstance(objectSpec);
            if (newInstance != null) {
                instances.put(0, newInstance);
                return new ObjectEnabler(objectSpec.id, instances, SimpleInstanceEnabler.class);
            }
        }
        return new ObjectEnabler(objectSpec.id, instances, SimpleInstanceEnabler.class);
    }

    protected LwM2mInstanceEnabler createInstance(ObjectSpec objectSpec) {
        Class<? extends LwM2mInstanceEnabler> clazz = classes.get(objectSpec.id);
        if (clazz == null)
            clazz = SimpleInstanceEnabler.class;

        LwM2mInstanceEnabler instance;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        instance.setObjectSpec(objectSpec);
        return instance;
    }
}
