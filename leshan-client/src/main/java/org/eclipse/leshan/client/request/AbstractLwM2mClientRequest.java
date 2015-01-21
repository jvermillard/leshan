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

public abstract class AbstractLwM2mClientRequest implements LwM2mClientRequest {
    protected static final long DEFAULT_TIMEOUT_MS = 500;

    private final long timeout;

    public AbstractLwM2mClientRequest() {
        this(DEFAULT_TIMEOUT_MS);
    }

    public AbstractLwM2mClientRequest(final long timeout) {
        this.timeout = timeout;
    }

    public final long getTimeout() {
        return timeout;
    }
}