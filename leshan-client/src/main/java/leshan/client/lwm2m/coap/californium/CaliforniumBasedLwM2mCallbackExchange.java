package leshan.client.lwm2m.coap.californium;

import leshan.client.lwm2m.exchange.LwM2mCallbackExchange;
import leshan.client.lwm2m.resource.LwM2mClientNode;
import leshan.client.lwm2m.response.LwM2mResponse;

import org.eclipse.californium.core.server.resources.CoapExchange;

public class CaliforniumBasedLwM2mCallbackExchange<T extends LwM2mClientNode> extends CaliforniumBasedLwM2mExchange implements LwM2mCallbackExchange<T> {

	private final Callback<T> callback;
	private T node;

	public CaliforniumBasedLwM2mCallbackExchange(final CoapExchange exchange, final Callback<T> callback) {
		super(exchange);
		this.callback = callback;
	}

	@Override
	public void respond(final LwM2mResponse response) {
		if (response.isSuccess()) {
			callback.onSuccess(node);
		} else {
			callback.onFailure();
		}
		super.respond(response);
	}

	@Override
	public void setNode(final T node) {
		this.node = node;
	}

}
