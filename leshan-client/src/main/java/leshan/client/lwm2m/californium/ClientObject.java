package leshan.client.lwm2m.californium;

import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT;

import java.util.concurrent.atomic.AtomicInteger;

import leshan.client.lwm2m.operation.CaliforniumBasedLwM2mExchange;
import leshan.client.lwm2m.resource.LinkFormattable;
import leshan.client.lwm2m.resource.LwM2mObject;
import leshan.client.lwm2m.resource.LwM2mObjectInstance;
import leshan.client.lwm2m.resource.LwM2mResourceDefinition;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class ClientObject extends ResourceBase implements LinkFormattable{

	private final LwM2mObject lwm2mObject;

	public ClientObject(final int objectId, final LwM2mResourceDefinition... definitions) {
		super(Integer.toString(objectId));
		if (definitions == null || definitions.length == 0) {
			throw new IllegalArgumentException("Must provide at least one resource definition");
		}
		new AtomicInteger(0);
		lwm2mObject = new LwM2mObject(definitions);
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_LINK_FORMAT){
			handleDiscover(exchange);
		}
		else{
			handleRead(exchange);
		}
	}

	private void handleDiscover(final CoapExchange exchange) {
		exchange.respond(CONTENT, asLinkFormat());
	}

	private void handleRead(final CoapExchange exchange) {
		lwm2mObject.handleRead(new CaliforniumBasedLwM2mExchange(exchange));
	}

	@Override
	public void handlePOST(final CoapExchange exchange) {
		final LwM2mObjectInstance instance = lwm2mObject.createInstance(new CaliforniumBasedLwM2mExchange(exchange));

		if (instance != null) {
			add(new ClientObjectInstance(instance.getId(), instance));
		}
	}

	@Override
	public String asLinkFormat() {
		final StringBuilder linkFormat = LinkFormat.serializeResource(this).append(LinkFormat.serializeAttributes(getAttributes()));
		for(final Resource child : getChildren()){
			for(final Resource grandchild : child.getChildren()){
				linkFormat.append(LinkFormat.serializeResource(grandchild));
			}
		}
		linkFormat.deleteCharAt(linkFormat.length() - 1);
		return linkFormat.toString();
	}

}
