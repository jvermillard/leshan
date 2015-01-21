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
package org.eclipse.leshan.server.client;

/**
 * Listen for client registration events.
 */
public interface ClientRegistryListener {

    /**
     * Invoked when a new client has been registered on the server.
     *
     * @param client
     */
    void registered(Client client);

    /**
     * Invoked when a client has been updated.
     *
     * @param clientUpdated the client after the update
     */
    void updated(Client clientUpdated);

    /**
     * Invoked when a new client has been unregistered from the server.
     *
     * @param client
     */
    void unregistered(Client client);
}
