package leshan.client.lwm2m.operation;

import leshan.client.lwm2m.californium.Callback;
import leshan.client.lwm2m.resource.LwM2mObjectInstance;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;

public class CaliforniumBasedLwM2mCreateExchange extends CaliforniumBasedLwM2mExchange implements LwM2mCreateExchange {

	private final Callback<LwM2mObjectInstance> callback;
	private LwM2mObjectInstance objectInstance;

	public CaliforniumBasedLwM2mCreateExchange(final CoapExchange exchange, final Callback<LwM2mObjectInstance> callback) {
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
	public void setObjectInstance(final LwM2mObjectInstance objectInstance) {
		this.objectInstance = objectInstance;
	}

}
