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
package org.eclipse.leshan.client.resource;

import static org.junit.Assert.assertEquals;

import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.client.response.WriteResponse;
import org.junit.Test;

public class WriteResponseTest {

    @Test
    public void canCreateSuccessfulResponse() {
        final WriteResponse response = WriteResponse.success();
        assertEquals(ResponseCode.CHANGED, response.getCode());
    }

    @Test
    public void canCreateFailureResponse() {
        final WriteResponse response = WriteResponse.failure();
        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
    }

}
