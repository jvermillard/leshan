/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Zebra Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.client.resource;

import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ValueResponse;

public abstract class LwM2mClientResource extends LwM2mClientNode {

    private int id;

    public int getId() {
        return id;
    }

    public final void setId(int id) {
        this.id = id;
    }

    public ValueResponse read() {
        return new ValueResponse(ResponseCode.METHOD_NOT_ALLOWED);
    }

    public LwM2mResponse execute() {
        return new LwM2mResponse(ResponseCode.METHOD_NOT_ALLOWED);
    }
}
