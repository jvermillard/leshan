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
     * Returns the list of all registered clients
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

}
