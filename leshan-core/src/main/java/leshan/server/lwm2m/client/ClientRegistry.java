/*
 * Copyright (c) 2013, Sierra Wireless
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of {{ project }} nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package leshan.server.lwm2m.client;

import java.util.Collection;

/**
 * A registry to access registered clients
 */
public interface ClientRegistry {

    /**
     * Retrieve a {@link Client} by endpoint.
     *
     * @param endpoint
     * @return the matching client or <code>null</code> if not found
     */
    Client get(String endpoint);

    /**
     * Return the list of all registered clients
     *
     * @return the registered clients
     */
    Collection<Client> allClients();

    /**
     * Add a new listener to be notified with client registration events.
     *
     * @param listener
     */
    void addListener(RegistryListener listener);

    /**
     * Remove a client registration listener.
     *
     * @param listener the listener to be removed
     */
    void removeListener(RegistryListener listener);

    /**
     * Register a new client
     *
     * @param client the client to register, identified by its end-point.
     * @return the previously registered client with this end-point or <code>null</code> if this is a new client.
     * @throws ClientRegistrationException when the client registration has failed
     */
    Client registerClient(Client client) throws ClientRegistrationException;

    /**
     * Update a client registration
     *
     * @param client the client containing the values to update
     * @return the registered client or <code>null</code>
     * @throws ClientRegistrationException when the registration update has failed
     */
    Client updateClient(ClientUpdate update) throws ClientRegistrationException;

    /**
     * De-register a client.
     *
     * @param registrationId the client registrationId
     * @return the previously registered client or <code>null</code>
     * @throws ClientRegistrationException when the client de-registation has failed
     */
    Client deregisterClient(String registrationId) throws ClientRegistrationException;
}
