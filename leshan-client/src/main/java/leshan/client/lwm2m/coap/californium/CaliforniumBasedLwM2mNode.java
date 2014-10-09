package leshan.client.lwm2m.coap.californium;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.LinkFormattable;
import leshan.client.lwm2m.resource.LwM2mClientNode;
import leshan.server.lwm2m.observation.ObserveSpec;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;

public abstract class CaliforniumBasedLwM2mNode<T extends LwM2mClientNode> extends CoapResource implements LinkFormattable {

	private static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	protected T node;

	public CaliforniumBasedLwM2mNode(int id, T node) {
		super(Integer.toString(id));
		setObservable(true);
		this.node = node;
	}

	@Override
	public void handleGET(final CoapExchange coapExchange) {
		if (coapExchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_LINK_FORMAT) {
			handleDiscover(coapExchange);
		} else {
			LwM2mExchange exchange = new CaliforniumBasedLwM2mExchange(coapExchange);
			if (exchange.isObserve()) {
				node.observe(exchange, service);
			}
			node.read(exchange);
		}
	}

	@Override
	public void handlePUT(final CoapExchange coapExchange) {
		LwM2mExchange exchange = new CaliforniumBasedLwM2mExchange(coapExchange);
		final ObserveSpec spec = exchange.getObserveSpec();
		if (spec != null) {
			node.writeAttributes(exchange, spec);
		} else {
			node.write(exchange);
		}
	}

	protected void handleDiscover(final CoapExchange exchange) {
		exchange.respond(ResponseCode.CONTENT, asLinkFormat(), MediaTypeRegistry.APPLICATION_LINK_FORMAT);
	}

}
