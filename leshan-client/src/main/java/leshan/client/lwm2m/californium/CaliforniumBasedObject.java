package leshan.client.lwm2m.californium;

import leshan.client.lwm2m.resource.LinkFormattable;
import leshan.client.lwm2m.resource.LwM2mClientObject;
import leshan.client.lwm2m.resource.LwM2mClientObjectDefinition;
import leshan.client.lwm2m.resource.LwM2mClientObjectInstance;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

public class CaliforniumBasedObject extends CoapResource implements LinkFormattable {

	private final LwM2mClientObject lwm2mObject;

	public CaliforniumBasedObject(final LwM2mClientObjectDefinition def) {
		super(Integer.toString(def.getId()));

		lwm2mObject = new LwM2mClientObject(def);
		if(def.isMandatory()) {
			createMandatoryObjectInstance(def);
		}
	}

	private void createMandatoryObjectInstance(final LwM2mClientObjectDefinition def) {
		LwM2mClientObjectInstance instance = lwm2mObject.createMandatoryInstance();
		onSuccessfulCreate(instance);
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
		lwm2mObject.handleCreate(new CaliforniumBasedLwM2mCreateExchange(exchange, getCreateCallback()));
	}

	private Callback<LwM2mClientObjectInstance> getCreateCallback() {
		return new Callback<LwM2mClientObjectInstance>() {

			@Override
			public void onSuccess(final LwM2mClientObjectInstance newInstance) {
				onSuccessfulCreate(newInstance);
			}

			@Override
			public void onFailure() {
			}

		};
	}

	public void onSuccessfulCreate(final LwM2mClientObjectInstance instance) {
		add(new CaliforniumBasedObjectInstance(instance.getId(), instance));
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
