package leshan.connector.californium.resource;

import leshan.server.lwm2m.resource.proxy.RequestProxy;

import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.Request;

public class CaliforniumRequestProxy extends RequestProxy {

	private final Request request;

	public CaliforniumRequestProxy(final Request request) {
		this.request = request;
	}

	@Override
	public boolean isConfirmable() {
		return Type.CON.equals(request.getType());
	}

}
