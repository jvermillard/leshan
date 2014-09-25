package leshan.client.lwm2m.resource;

import org.eclipse.californium.core.server.resources.CoapExchange;

public interface ClientObservable {

	void createObservation(ClientObservable observable, CoapExchange exchange);

}
