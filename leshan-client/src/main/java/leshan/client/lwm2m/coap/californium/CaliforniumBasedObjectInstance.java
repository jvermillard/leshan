package leshan.client.lwm2m.coap.californium;

import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.LinkFormattable;
import leshan.client.lwm2m.resource.LwM2mClientObjectInstance;
import leshan.client.lwm2m.resource.LwM2mClientResource;
import leshan.client.lwm2m.response.WriteResponse;
import leshan.server.lwm2m.observation.ObserveSpec;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

public class CaliforniumBasedObjectInstance extends CoapResource implements LinkFormattable {

	private final LwM2mClientObjectInstance lwm2mObjectInstance;
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

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
			CaliforniumBasedLwM2mExchange lwm2mExchange = new CaliforniumBasedLwM2mExchange(exchange);
			if (lwm2mExchange.isObserve()) {
				lwm2mObjectInstance.observe(lwm2mExchange, service);
			}
			lwm2mObjectInstance.read(lwm2mExchange);
		}
	}

	@Override
	public void handlePUT(final CoapExchange coapExchange) {
		LwM2mExchange exchange = new CaliforniumBasedLwM2mExchange(coapExchange);
		final ObserveSpec spec = exchange.getObserveSpec();
		if (spec != null) {
			lwm2mObjectInstance.writeAttributes(exchange, spec);
		} else {
			exchange.respond(WriteResponse.notAllowed());
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