package leshan.client.lwm2m.resource;

import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leshan.client.lwm2m.operation.CaliforniumBasedLwM2mExchange;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.ReadResponse;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

class ClientResource extends ResourceBase implements LinkFormattable, ClientObservable, Notifier{

	private static final int IS_OBSERVE = 0;
	private final LwM2mResource resource;
	private final Map<ClientObservable, String> observationTokens;
	private final int id;

	public ClientResource(final int id, final LwM2mResource executable) {
		super(Integer.toString(id));
		this.id = id;
		setObservable(true);

		this.resource = executable;

		observationTokens = new ConcurrentHashMap<>();
	}

	public int getId() {
		return id;
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		if (exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_LINK_FORMAT) {
			handleDiscover(exchange);
		} else {
			handleRead(exchange);
		}
	}

	private void handleDiscover(final CoapExchange exchange) {
		exchange.respond(CONTENT, asLinkFormat());
	}

	private void handleRead(final CoapExchange exchange) {
		if (isNotifyRead(exchange)) {
			handleObserveNotifyRead(exchange);
		} else {
			handleNormalRead(new CaliforniumBasedLwM2mExchange(exchange));
		}

	}

	public void handleNormalRead(final LwM2mExchange exchange) {
		resource.read(exchange);
	}

	private void handleObserveNotifyRead(final CoapExchange exchange) {
		resource.read(new CaliforniumBasedLwM2mExchange(exchange));
	}

	private boolean isNotifyRead(final CoapExchange exchange) {
		for (final String t : observationTokens.values()) {
			if (t.equals(exchange.advanced().getRequest().getTokenString())) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("unused")
	private void handleObserve(final CoapExchange exchange) {
		if (exchange.getRequestOptions().hasObserve() && exchange.getRequestOptions().getObserve() == IS_OBSERVE) {
			createObservation(this, exchange);
		}
	}

	@Override
	public void createObservation(final ClientObservable observable, final CoapExchange exchange) {
		if(!observationTokens.containsKey(observable)){
			observationTokens.put(observable, exchange.advanced().getRequest().getTokenString());

			if(observationTokens.size() == 1){
				resource.observe(this);
			}
		}
		else{
			//Is there anything needed to be done if we already have this?
		}
	}

	@Override
	public void handlePUT(final CoapExchange exchange) {
		resource.write(new CaliforniumBasedLwM2mExchange(exchange));
	}

	@Override
	public void handlePOST(final CoapExchange exchange) {
		resource.execute(new CaliforniumBasedLwM2mExchange(exchange));
	}

	@Override
	public String asLinkFormat() {
		final StringBuilder linkFormat = LinkFormat.serializeResource(this).append(LinkFormat.serializeAttributes(getAttributes()));

		linkFormat.deleteCharAt(linkFormat.length() - 1);

		return linkFormat.toString();
	}

	@Override
	public void notify(final ReadResponse notification) {
		notifyObserverRelations();
	}

}