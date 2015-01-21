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
package org.eclipse.leshan.client.resource.decimal;

import org.eclipse.leshan.client.exchange.LwM2mExchange;
import org.eclipse.leshan.client.resource.TypedLwM2mExchange;

public class FloatLwM2mExchange extends TypedLwM2mExchange<Float> {

    public FloatLwM2mExchange(final LwM2mExchange exchange) {
        super(exchange);
    }

    @Override
    protected Float convertFromBytes(final byte[] value) {
        return Float.parseFloat(new String(value));
    }

    @Override
    protected byte[] convertToBytes(final Float value) {
        return Float.toString(value).getBytes();
    }

}
