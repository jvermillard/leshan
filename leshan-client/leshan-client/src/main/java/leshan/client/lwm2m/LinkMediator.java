package leshan.client.lwm2m;

import java.util.Map;

import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.network.Endpoint;
import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.client.lwm2m.bootstrap.BootstrapUplink;
import leshan.client.lwm2m.response.OperationResponse;

/**
 * The Brain of the Library
 * Contains the mappings Downlink/Uplinks/CoAPEndpoints
 *
 */
public class LinkMediator implements BootstrapUplink{
	//TBD
	//TODO:  should this actually go in the ClientFactory????
	Map<Endpoint, BootstrapDownlink> linkEndpointMap;

	public OperationResponse bootstrap() {
		// TODO Auto-generated method stub

		final Endpoint endpoint = null; //Something;
		//Store the endpoint

		Request.newPost().send(endpoint);

		return null;
	}

}
