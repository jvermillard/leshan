package leshan.client.lwm2m.californium;

import leshan.client.lwm2m.operation.LwM2mCreateExchange;
import leshan.client.lwm2m.operation.LwM2mResponse;
import leshan.client.lwm2m.resource.LwM2mClientObjectInstance;

import org.eclipse.californium.core.server.resources.CoapExchange;

public class CaliforniumBasedLwM2mCreateExchange extends CaliforniumBasedLwM2mExchange implements LwM2mCreateExchange {

	private final Callback<LwM2mClientObjectInstance> callback;
	private LwM2mClientObjectInstance objectInstance;

	public CaliforniumBasedLwM2mCreateExchange(final CoapExchange exchange, final Callback<LwM2mClientObjectInstance> callback) {
		super(exchange);
		this.callback = callback;
	}

	@Override
	public void respond(final LwM2mResponse response) {
		if (response.isSuccess()) {
			callback.onSuccess(objectInstance);
		} else {
			callback.onFailure();
		}
		super.respond(response);
	}

	@Override
	public void setObjectInstance(final LwM2mClientObjectInstance objectInstance) {
		this.objectInstance = objectInstance;
	}

}
