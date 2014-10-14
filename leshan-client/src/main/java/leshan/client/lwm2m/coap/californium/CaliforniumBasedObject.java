package leshan.client.lwm2m.coap.californium;

import leshan.client.lwm2m.resource.LwM2mClientObject;
import leshan.client.lwm2m.resource.LwM2mClientObjectDefinition;
import leshan.client.lwm2m.resource.LwM2mClientObjectInstance;

import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

public class CaliforniumBasedObject extends CaliforniumBasedLwM2mNode<LwM2mClientObject> {

	public CaliforniumBasedObject(final LwM2mClientObjectDefinition def) {
		super(def.getId(), new LwM2mClientObject(def));

		if(def.isMandatory()) {
			createMandatoryObjectInstance(def);
		}
	}

	private void createMandatoryObjectInstance(final LwM2mClientObjectDefinition def) {
		LwM2mClientObjectInstance instance = node.createMandatoryInstance();
		onSuccessfulCreate(instance);
	}

	@Override
	public void handlePOST(final CoapExchange exchange) {
		node.createInstance(new CaliforniumBasedLwM2mCallbackExchange<LwM2mClientObjectInstance>(exchange, getCreateCallback()));
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
		node.onSuccessfulCreate(instance);
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
