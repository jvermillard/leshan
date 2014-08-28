package leshan.client.lwm2m.resource;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;

public class ExecutableResource extends ClientResource {

	private final ExecuteListener listener;

	public ExecutableResource(final int id, final ExecuteListener listener) {
		super(id, null);
		this.listener = listener;
	}

	@Override
	public void handlePOST(final CoapExchange exchange) {
		listener.execute(Integer.parseInt(getParent().getParent().getName()),
				Integer.parseInt(getParent().getName()),
				Integer.parseInt(getName()));
		exchange.respond(ResponseCode.CHANGED);
	}

	@Override
	public boolean isReadable() {
		return false;
	}

}
