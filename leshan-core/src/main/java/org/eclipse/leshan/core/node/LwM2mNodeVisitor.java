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
 * A visitor to visit an object, an object instance, or a resource.
 */
public interface LwM2mNodeVisitor {

    void visit(LwM2mObject object);

    void visit(LwM2mObjectInstance instance);

    void visit(LwM2mResource resource);

}
