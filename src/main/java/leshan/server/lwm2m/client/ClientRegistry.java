/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
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
     * Registers a new client.
     * 
     * If the registry already contains a client with the same end-point identifier,
     * the original client object is replaced with the new one and listeners are
     * notified of both the de-registration of the original client and the registration
     * of the new one.
     *  
     * @param client the client to register, identified by its end-point.
     * @return the previously registered client with this end-point or <code>null</code> if this is a new client.
     * @throws ClientRegistrationException if the registration has failed
     */
    Client registerClient(Client client) throws ClientRegistrationException;

    /**
     * De-registers a client.
     * 
     * @param registrationId the client registrationId
     * @return the previously registered client or <code>null</code>
     * @throws ClientRegistrationException if the de-registration has failed
     */
    Client deregisterClient(String registrationId) throws ClientRegistrationException;
    
    /**
     * Notifies all registered listeners about an updated client.
     * 
     * @param updatedClient the client
     */
    void notifyListeners(Client updatedClient);
}
