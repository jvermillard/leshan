package leshan.client.lwm2m.californium;

import leshan.client.lwm2m.resource.LinkFormattable;
import leshan.client.lwm2m.resource.LwM2mObject;
import leshan.client.lwm2m.resource.LwM2mObjectDefinition;
import leshan.client.lwm2m.resource.LwM2mObjectInstance;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

public class ClientObject extends CoapResource implements LinkFormattable {

	private final LwM2mObject lwm2mObject;

	public ClientObject(final LwM2mObjectDefinition def) {
		super(Integer.toString(def.getId()));

		lwm2mObject = new LwM2mObject(def);
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_LINK_FORMAT){
			handleDiscover(exchange);
		} else {
			handleRead(exchange);
		}
	}

	private void handleDiscover(final CoapExchange exchange) {
		exchange.respond(ResponseCode.CONTENT, asLinkFormat(), MediaTypeRegistry.APPLICATION_LINK_FORMAT);
	}

	private void handleRead(final CoapExchange exchange) {
		lwm2mObject.handleRead(new CaliforniumBasedLwM2mExchange(exchange));
	}

	@Override
	public void handlePOST(final CoapExchange exchange) {
		final Callback<LwM2mObjectInstance> callback = new Callback<LwM2mObjectInstance>() {

			@Override
			public void onSuccess(final LwM2mObjectInstance newInstance) {
				onSuccessfulCreate(newInstance);
			}

			@Override
			public void onFailure() {
			}

		};
		lwm2mObject.handleCreate(new CaliforniumBasedLwM2mCreateExchange(exchange, callback));
	}

	public void onSuccessfulCreate(final LwM2mObjectInstance instance) {
		add(new ClientObjectInstance(instance.getId(), instance));
		lwm2mObject.onSuccessfulCreate(instance);
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
