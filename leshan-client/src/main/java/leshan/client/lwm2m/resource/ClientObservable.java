package leshan.client.lwm2m.resource;

import ch.ethz.inf.vs.californium.server.resources.CoapExchange;

public interface ClientObservable {

	void createObservation(ClientObservable observable, CoapExchange exchange);

}
