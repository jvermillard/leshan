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
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ValueResponse;

public interface LwM2mInstanceEnabler {

    void setObjectSpec(ObjectSpec objectSpec);

    void addResourceChangedListener(ResourceChangedListener listener);

    void removeResourceChangedListener(ResourceChangedListener listener);

    ValueResponse read(int resourceid);

    LwM2mResponse write(int resourceid, LwM2mResource value);

    LwM2mResponse execute(int resourceid, byte[] params);

}
