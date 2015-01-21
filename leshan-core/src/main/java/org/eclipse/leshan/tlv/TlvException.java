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
package org.eclipse.leshan.tlv;

public class TlvException extends Exception {

    public TlvException(String message) {
        super(message);
    }

    public TlvException(String message, Exception cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = 9017593873541376092L;

}
