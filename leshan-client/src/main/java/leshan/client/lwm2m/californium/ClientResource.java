package leshan.client.lwm2m.californium;

import leshan.client.lwm2m.resource.LinkFormattable;
import leshan.client.lwm2m.resource.LwM2mResource;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;

class ClientResource extends CoapResource implements LinkFormattable {

	private final LwM2mResource lwm2mResource;
	private final int id;

	public ClientResource(final int id, final LwM2mResource lwM2mResource) {
		super(Integer.toString(id));
		this.id = id;
		setObservable(true);

		this.lwm2mResource = lwM2mResource;
	}

	public int getId() {
		return id;
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		if (isDiscover(exchange)) {
			handleDiscover(exchange);
		} else {
			handleRead(exchange);
		}
	}

	private boolean isDiscover(final CoapExchange exchange) {
		return exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_LINK_FORMAT;
	}

	private void handleDiscover(final CoapExchange exchange) {
		exchange.respond(ResponseCode.CONTENT, asLinkFormat());
	}

	private void handleRead(final CoapExchange exchange) {
		// TODO: Put resource check for permissions
		// TODO: Put resource check for valid op
		lwm2mResource.read(new CaliforniumBasedLwM2mExchange(exchange));
	}

	@Override
	public void handlePUT(final CoapExchange exchange) {
		lwm2mResource.write(new CaliforniumBasedLwM2mExchange(exchange));
	}

	@Override
	public void handlePOST(final CoapExchange exchange) {
		lwm2mResource.execute(new CaliforniumBasedLwM2mExchange(exchange));
	}

	@Override
	public String asLinkFormat() {
		final StringBuilder linkFormat = LinkFormat.serializeResource(this).append(LinkFormat.serializeAttributes(getAttributes()));

		linkFormat.deleteCharAt(linkFormat.length() - 1);

		return linkFormat.toString();
	}

}