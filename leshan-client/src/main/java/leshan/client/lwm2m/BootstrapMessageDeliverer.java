package leshan.client.lwm2m;

import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.server.lwm2m.exception.InvalidUriException;
import leshan.server.lwm2m.message.ResourceSpec;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Exchange;
import ch.ethz.inf.vs.californium.server.MessageDeliverer;

public class BootstrapMessageDeliverer implements MessageDeliverer {

	private final BootstrapDownlink downlink;

	public BootstrapMessageDeliverer(final BootstrapDownlink downlink) {
		this.downlink = downlink;
	}

	@Override
	public void deliverRequest(final Exchange exchange) {
		try {
			final ResourceSpec lwm2mUri = ResourceSpec.of(exchange.getRequest().getURI());
			downlink.write(lwm2mUri.getObjectId(), lwm2mUri.getObjectInstanceId(), lwm2mUri.getResourceId());
			exchange.sendResponse(new Response(ResponseCode.CHANGED));
		} catch (final InvalidUriException e) {
			exchange.sendResponse(new Response(ResponseCode.BAD_REQUEST));
		} catch (final Exception e) {
			exchange.sendResponse(new Response(ResponseCode.INTERNAL_SERVER_ERROR));
		}
	}

	@Override
	public void deliverResponse(final Exchange exchange, final Response response) {
		// TODO: DOES NOTHING????
		throw new UnsupportedOperationException("Cannot deliver response from BootstrapMessageDeliverer");
	}

}
