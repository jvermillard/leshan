package leshan.client.lwm2m.coap.californium;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.LinkFormattable;
import leshan.client.lwm2m.resource.LwM2mClientObject;
import leshan.client.lwm2m.resource.LwM2mClientObjectDefinition;
import leshan.client.lwm2m.resource.LwM2mClientObjectInstance;
import leshan.client.lwm2m.response.WriteResponse;
import leshan.server.lwm2m.observation.ObserveSpec;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

public class CaliforniumBasedObject extends CoapResource implements LinkFormattable {

	private final LwM2mClientObject lwm2mObject;
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

	public CaliforniumBasedObject(final LwM2mClientObjectDefinition def) {
		super(Integer.toString(def.getId()));

		lwm2mObject = new LwM2mClientObject(def);
		if(def.isMandatory()) {
			createMandatoryObjectInstance(def);
		}

		setObservable(true);
	}

	private void createMandatoryObjectInstance(final LwM2mClientObjectDefinition def) {
		LwM2mClientObjectInstance instance = lwm2mObject.createMandatoryInstance();
		onSuccessfulCreate(instance);
	}

	@Override
	public void handleGET(final CoapExchange coapExchange) {
		if(coapExchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_LINK_FORMAT){
			handleDiscover(coapExchange);
		} else {
			CaliforniumBasedLwM2mExchange exchange = new CaliforniumBasedLwM2mExchange(coapExchange);
			if (exchange.isObserve()) {
				lwm2mObject.observe(exchange, service);
			}
			lwm2mObject.read(exchange);
		}
	}

	private void handleDiscover(final CoapExchange exchange) {
		exchange.respond(ResponseCode.CONTENT, asLinkFormat(), MediaTypeRegistry.APPLICATION_LINK_FORMAT);
	}

	@Override
	public void handlePUT(final CoapExchange coapExchange) {
		LwM2mExchange exchange = new CaliforniumBasedLwM2mExchange(coapExchange);
		final ObserveSpec spec = exchange.getObserveSpec();
		if (spec != null) {
			lwm2mObject.writeAttributes(exchange, spec);
		} else {
			exchange.respond(WriteResponse.notAllowed());
		}
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
