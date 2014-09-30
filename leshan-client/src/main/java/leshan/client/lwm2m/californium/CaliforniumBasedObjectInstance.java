package leshan.client.lwm2m.californium;

import java.util.Map.Entry;

import leshan.client.lwm2m.resource.LinkFormattable;
import leshan.client.lwm2m.resource.LwM2mClientObjectInstance;
import leshan.client.lwm2m.resource.LwM2mClientResource;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

public class CaliforniumBasedObjectInstance extends CoapResource implements LinkFormattable {

	private final LwM2mClientObjectInstance lwm2mObjectInstance;

	public CaliforniumBasedObjectInstance(final int instanceId, final LwM2mClientObjectInstance lwm2mObjectInstance) {
		super(Integer.toString(instanceId));
		this.lwm2mObjectInstance = lwm2mObjectInstance;
		for (final Entry<Integer, LwM2mClientResource> entry : lwm2mObjectInstance.getAllResources().entrySet()) {
			final Integer resourceId = entry.getKey();
			final LwM2mClientResource resource = entry.getValue();
			add(new CaliforniumBasedResource(resourceId, resource));
		}
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_LINK_FORMAT){
			handleDiscover(exchange);
		} else {
			lwm2mObjectInstance.handleRead(new CaliforniumBasedLwM2mExchange(exchange));
		}
	}

	private void handleDiscover(final CoapExchange exchange) {
		exchange.respond(ResponseCode.CONTENT, asLinkFormat(), MediaTypeRegistry.APPLICATION_LINK_FORMAT);
	}

	@Override
	public void handleDELETE(final CoapExchange exchange) {
		getParent().remove(this);

		exchange.respond(ResponseCode.DELETED);
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