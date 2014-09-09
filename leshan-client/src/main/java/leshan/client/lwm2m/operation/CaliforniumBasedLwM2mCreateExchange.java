package leshan.client.lwm2m.operation;

import java.util.concurrent.atomic.AtomicBoolean;

import leshan.client.lwm2m.californium.Callback;
import leshan.client.lwm2m.resource.LwM2mObjectInstance;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;

public class CaliforniumBasedLwM2mCreateExchange extends CaliforniumBasedLwM2mExchange implements LwM2mCreateExchange {

	private final Callback<LwM2mObjectInstance> callback;
	private LwM2mObjectInstance objectInstance;
	private final AtomicBoolean hasResponded;

	public CaliforniumBasedLwM2mCreateExchange(final CoapExchange exchange, final Callback<LwM2mObjectInstance> callback) {
		super(exchange);
		this.callback = callback;
		hasResponded = new AtomicBoolean();
	}

	@Override
	public void respond(final LwM2mResponse response) {
		super.respond(response);
		hasResponded.set(true);
		runCallback();
	}

	@Override
	public void setObjectInstance(final LwM2mObjectInstance objectInstance) {
		this.objectInstance = objectInstance;
		runCallback();
	}

	private void runCallback() {
		if (objectInstance != null && hasResponded.get()) {
			callback.onSuccess(objectInstance);
		}
	}

}
