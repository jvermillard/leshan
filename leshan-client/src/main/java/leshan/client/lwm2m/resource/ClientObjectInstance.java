package leshan.client.lwm2m.resource;

import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT;
import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.DELETED;

import java.util.Map;

import leshan.client.lwm2m.operation.AggregatedLwM2mExchange;
import leshan.client.lwm2m.operation.CaliforniumBasedLwM2mExchange;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.LwM2mResourceReadResponseAggregator;
import leshan.client.lwm2m.operation.LwM2mResponseAggregator;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

class ClientObjectInstance extends ResourceBase implements LinkFormattable{

	public ClientObjectInstance(final int instanceID, final Map<Integer, ClientResource> resources) {
		super(Integer.toString(instanceID));
		for(final Map.Entry<Integer, ClientResource> entry : resources.entrySet()){
			add(entry.getValue());
		}
	}

	public int getId() {
		return Integer.parseInt(getName());
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_LINK_FORMAT){
			handleDiscover(exchange);
		} else {
			handleNormalRead(new CaliforniumBasedLwM2mExchange(exchange));
		}
	}

	private void handleDiscover(final CoapExchange exchange) {
		exchange.respond(CONTENT, asLinkFormat());
	}

	public void handleNormalRead(final LwM2mExchange exchange) {
		final LwM2mResponseAggregator aggr = new LwM2mResourceReadResponseAggregator(
				exchange,
				getChildren().size());
		for (final Resource child : getChildren()) {
			final ClientResource res = (ClientResource)child;
			res.handleNormalRead(new AggregatedLwM2mExchange(aggr, res.getId()));
		}
	}

	@Override
	public void handleDELETE(final CoapExchange exchange) {
		getParent().remove(this);

		exchange.respond(DELETED);
	}

	@Override
	public String asLinkFormat() {
		final StringBuilder linkFormat = LinkFormat.serializeResource(this).append(LinkFormat.serializeAttributes(getAttributes()));
		for(final Resource child : getChildren()) {
			linkFormat.append(LinkFormat.serializeResource(child));
		}
		linkFormat.deleteCharAt(linkFormat.length() - 1);

		return linkFormat.toString();
	}

}