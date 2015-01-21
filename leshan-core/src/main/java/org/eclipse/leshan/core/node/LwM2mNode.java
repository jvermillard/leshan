/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.core.node;

/**
 * A node in the LWM2M resource tree: Objects, Object instances and Resources.
 */
public interface LwM2mNode {

    /**
     * @return the node identifier
     */
    int getId();

    /**
     * Accept a visitor for this node.
     */
    void accept(LwM2mNodeVisitor visitor);

}
