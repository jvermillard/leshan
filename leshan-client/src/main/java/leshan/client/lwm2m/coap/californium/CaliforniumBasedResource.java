package leshan.client.lwm2m.coap.californium;

import leshan.client.lwm2m.resource.LinkFormattable;
import leshan.client.lwm2m.resource.LwM2mClientResource;

import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.server.resources.CoapExchange;

class CaliforniumBasedResource extends CaliforniumBasedLwM2mNode<LwM2mClientResource> implements LinkFormattable {

	public CaliforniumBasedResource(final int id, final LwM2mClientResource lwM2mResource) {
		super(id, lwM2mResource);
	}

	@Override
	public void handlePOST(final CoapExchange exchange) {
		node.execute(new CaliforniumBasedLwM2mExchange(exchange));
	}

	@Override
	public String asLinkFormat() {
		final StringBuilder linkFormat = LinkFormat.serializeResource(this).append(LinkFormat.serializeAttributes(getAttributes()));

		linkFormat.deleteCharAt(linkFormat.length() - 1);

		return linkFormat.toString();
	}

}