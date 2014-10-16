package leshan.connector.californium.resource;

import java.net.InetSocketAddress;
import java.util.List;

import leshan.server.lwm2m.client.BindingMode;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientUpdate;
import leshan.server.lwm2m.client.LinkObject;
import leshan.server.lwm2m.impl.security.SecureEndpoint;
import leshan.server.lwm2m.resource.proxy.ExchangeProxy;
import leshan.server.lwm2m.resource.proxy.RequestProxy;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Request;
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
	public void respond(
			final leshan.server.lwm2m.request.CoapResponseCode.ResponseCode code, final String... errorMessage) {
		ResponseCode caCode = null;

		switch(code){
		case BAD_REQUEST:
			caCode = ResponseCode.BAD_REQUEST;
			break;
		case CREATED:
			caCode = ResponseCode.CREATED;
			break;
		case NOT_FOUND:
			caCode = ResponseCode.NOT_FOUND;
			break;
		case CHANGED:
			caCode = ResponseCode.CHANGED;
			break;
		case DELETED:
			caCode = ResponseCode.DELETED;
			break;
		case CONTENT:
			caCode = ResponseCode.CONTENT;
			break;
		default:
			caCode = ResponseCode.INTERNAL_SERVER_ERROR;

		}

		if(errorMessage == null || errorMessage.length == 0){
			exchange.respond(caCode);
		}
		else{
			exchange.respond(caCode, errorMessage[0]);
		}

	}

	@Override
	public InetSocketAddress getEndpointAddress() {
		return exchange.advanced().getEndpoint().getAddress();
	}

	@Override
	public boolean isUsingSecureEndpoint() {
		return exchange.advanced().getEndpoint() instanceof SecureEndpoint;
	}

	@Override
	public String getPskIdentity() {
		if (exchange.advanced().getEndpoint() instanceof SecureEndpoint) {
			return ((SecureEndpoint) exchange.advanced().getEndpoint()).getPskIdentity(exchange.advanced().getRequest());
		}

		return null;
	}

	@Override
	public void killTlsSession() {
		final Request californiumRequest = exchange.advanced().getRequest();

		((SecureEndpoint) exchange.advanced().getEndpoint()).getDTLSConnector().close(
				new InetSocketAddress(californiumRequest.getSource(), californiumRequest.getSourcePort()));
	}

	@Override
	public Client createNewClient(final String registrationId, final String endpoint, final String lwVersion,
			final Long lifetime, final String smsNumber, final BindingMode binding,
			final LinkObject[] objectLinks, final InetSocketAddress registrationEndpoint) {
		final Request californiumRequest = exchange.advanced().getRequest();

		final Client client = new Client(registrationId, endpoint, californiumRequest.getSource(), californiumRequest.getSourcePort(),
				lwVersion, lifetime, smsNumber, binding, objectLinks, registrationEndpoint);

		return client;
	}

	@Override
	public void setLocationPath(final String locationPath) {
		exchange.setLocationPath(locationPath);
	}

	@Override
	public List<String> getURIPaths() {
		return exchange.getRequestOptions().getURIPaths();
	}

	@Override
	public ClientUpdate createNewClientUpdate(final String registrationId, final Long lifetime,
			final String smsNumber, final BindingMode binding, final LinkObject[] objectLinks) {
		final Request californiumRequest = exchange.advanced().getRequest();
		
		final ClientUpdate client = new ClientUpdate(registrationId, californiumRequest.getSource(), californiumRequest.getSourcePort(), lifetime,
		smsNumber, binding, objectLinks);
		
		return client;
		
	}

}
