/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Zebra Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.client.request;

public interface LwM2mClientRequestVisitor {
    void visit(RegisterRequest request);

    void visit(DeregisterRequest request);

    void visit(UpdateRequest updateRequest);

    void visit(BootstrapRequest bootstrapRequest);

}
