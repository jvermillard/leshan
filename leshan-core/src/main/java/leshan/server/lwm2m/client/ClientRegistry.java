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
     * Retrieves a registered client by end-point.
     * 
     * @param endpoint
     * @return the matching client or <code>null</code> if not found
     */
    Client get(String endpoint);

    /**
     * Returns an unmodifiable list of all registered clients.
     * 
     * @return the registered clients
     */
    Collection<Client> allClients();

    /**
     * Adds a new listener to be notified with client registration events.
     * 
     * @param listener
     */
    void addListener(RegistryListener listener);

    /**
     * Removes a client registration listener.
     * 
     * @param listener the listener to be removed
     */
    void removeListener(RegistryListener listener);

    /**
     * Registers a new client.
     * 
     * An implementation must notify all registered listeners as part of
     * processing the registration request.
     * 
     * @param client the client to register, identified by its end-point.
     * @return any <em>stale</em> registration information for the given
     *         client's end-point name or <code>null</code> if no stale
     *         registration info exists for the end-point. This may happen, if a
     *         client somehow loses track of its registration status with this
     *         server and simply starts over with a new registration request in
     *         order to remedy the situation. According to the LWM2M spec an
     *         implementation must remove the <em>stale</em> registration
     *         information in this case.
     * @throws ClientRegistrationException if registration fails
     */
    Client registerClient(Client client) throws ClientRegistrationException;

    /**
     * Updates registration properties for a given client.
     * 
     * @param client the registration properties to update
     * @return the updated registered client or <code>null</code> if no client
     *         is registered under the given end-point name
     * @throws ClientRegistrationException when the registration update has
     *             failed
     */
    Client updateClient(ClientUpdate update) throws ClientRegistrationException;

    /**
     * De-registers a client.
     * 
     * @param registrationId the client registrationId
     * @return the previously registered client or <code>null</code> if no
     *         client is registered under the given ID
     * @throws ClientRegistrationException when the client de-registation has
     *             failed
     */
    Client deregisterClient(String registrationId) throws ClientRegistrationException;
}
