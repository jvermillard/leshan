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
package org.eclipse.leshan.client.exchange;

import org.eclipse.leshan.client.resource.LwM2mClientNode;

public interface LwM2mCallbackExchange<T extends LwM2mClientNode> extends LwM2mExchange {

    void setNode(T node);

}
