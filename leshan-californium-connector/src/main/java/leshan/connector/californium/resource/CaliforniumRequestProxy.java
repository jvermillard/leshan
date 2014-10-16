package leshan.connector.californium.resource;

import java.util.List;

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

	@Override
	public List<String> getURIQueries() {
		return request.getOptions().getURIQueries();
	}

	@Override
	public boolean hasPayload() {
		return request.getPayload() != null;
	}

	@Override
	public byte[] getPayload() {
		return request.getPayload();
	}

}
