package leshan.client.lwm2m;

import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.server.lwm2m.message.ResourceSpec;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Exchange;
import ch.ethz.inf.vs.californium.server.MessageDeliverer;

public class BootstrapMessageDeliverer implements MessageDeliverer {

	private final BootstrapDownlink downlink;

	public BootstrapMessageDeliverer(BootstrapDownlink downlink) {
		this.downlink = downlink;
	}

	@Override
	public void deliverRequest(Exchange exchange) {
		switch(exchange.getRequest().getCode()) {
			case PUT:
				ResourceSpec lwm2mUri = new ResourceSpec(exchange.getRequest().getURI());
				downlink.write(lwm2mUri.getObjectId(), lwm2mUri.getObjectInstanceId(), lwm2mUri.getResourceId());
				break;
			case DELETE:
				
				break;
			default:
				
		}
	}

	@Override
	public void deliverResponse(Exchange exchange, Response response) {
		// TODO Auto-generated method stub

	}

}
