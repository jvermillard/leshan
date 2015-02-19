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

import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.objectspec.ObjectSpec;

public interface LwM2mInstanceEnabler {

    void setObjectSpec(ObjectSpec objectSpec);

    void addResourceChangedListener(ResourceChangedListener listener);

    void removeResourceChangedListener(ResourceChangedListener listener);

    LwM2mResource read(int resourceid);

    void write(int resourceid, LwM2mResource value);

    void execute(int resourceid, byte[] params);

}
