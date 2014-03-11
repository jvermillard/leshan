package leshan.server.lwm2m.osgi;

import java.util.Collection;

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.client.ClientUpdate;
import leshan.server.lwm2m.client.RegistryListener;

/**
 * Uses the OSGi service registry as back end for managing the registered clients.
 * Takes the registrered Client object and wraps it into an instance of ??? implementing
 * the LWM2MDevice interface
 * 
 * @author wa20230
 *
 */
public class OsgiBasedClientRegistry implements ClientRegistry {

	@Override
	public Client get(String endpoint) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Client> allClients() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addListener(RegistryListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeListener(RegistryListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Client registerClient(Client client) {
		// TODO instantiate LWM2MDevice as wrapper around Client object and register as
		// DEVICE in OSGi registry
		return null;
	}

	@Override
	public Client deregisterClient(String registrationId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Client updateClient(ClientUpdate update) {
		// TODO Auto-generated method stub
		return null;
	}

}
