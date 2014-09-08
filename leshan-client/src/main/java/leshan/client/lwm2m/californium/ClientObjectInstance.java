package leshan.client.lwm2m.californium;

import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT;
import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.DELETED;

import java.util.Map.Entry;

import leshan.client.lwm2m.operation.CaliforniumBasedLwM2mExchange;
import leshan.client.lwm2m.resource.LinkFormattable;
import leshan.client.lwm2m.resource.LwM2mObjectInstance;
import leshan.client.lwm2m.resource.LwM2mResource;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class ClientObjectInstance extends ResourceBase implements LinkFormattable {

	private final LwM2mObjectInstance lwm2mObjectInstance;

	public ClientObjectInstance(final int instanceID, final LwM2mObjectInstance lwm2mObjectInstance) {
		super(Integer.toString(instanceID));
		this.lwm2mObjectInstance = lwm2mObjectInstance;
		for (final Entry<Integer, LwM2mResource> entry : lwm2mObjectInstance.getAllResources().entrySet()) {
			final Integer resourceId = entry.getKey();
			final LwM2mResource resource = entry.getValue();
			add(new ClientResource(resourceId, resource));
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
			lwm2mObjectInstance.handleNormalRead(new CaliforniumBasedLwM2mExchange(exchange));
		}
	}

	private void handleDiscover(final CoapExchange exchange) {
		exchange.respond(CONTENT, asLinkFormat());
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