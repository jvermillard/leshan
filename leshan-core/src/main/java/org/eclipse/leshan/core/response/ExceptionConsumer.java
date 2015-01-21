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
package org.eclipse.leshan.core.response;

/**
 * Functional interface receiving errors produced by a LWM2M request
 */
public interface ExceptionConsumer {

    /**
     * Called when a exception occurs during a request
     * @param e the produced exception
     */
    void accept(Exception e);
}
