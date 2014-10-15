package leshan.server.lwm2m.resource.californium;

import leshan.server.lwm2m.resource.proxy.ExchangeProxy;
import leshan.server.lwm2m.resource.proxy.RequestProxy;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class CaliforniumExchangeProxy extends ExchangeProxy {

	private final CoapExchange exchange;
	private final RequestProxy request;

	public CaliforniumExchangeProxy(final CoapExchange exchange) {
		this.exchange = exchange;
		this.request = new CaliforniumRequestProxy(exchange.advanced().getRequest());
	}

	@Override
	public RequestProxy getRequest() {
		return request;
	}

	@Override
	public void respondWithBadRequest() {
		exchange.respond(ResponseCode.BAD_REQUEST);
		
	}

}
