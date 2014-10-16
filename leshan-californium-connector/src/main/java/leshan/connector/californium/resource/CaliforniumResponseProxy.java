package leshan.connector.californium.resource;

import leshan.server.lwm2m.request.CoapResponseCode.ResponseCode;
import leshan.server.lwm2m.resource.proxy.ResponseProxy;

import org.eclipse.californium.core.coap.Response;

public class CaliforniumResponseProxy extends ResponseProxy {

	private final Response response;

	public CaliforniumResponseProxy(final Response response, final ResponseCode code) {
		super(code);
		this.response = response;
	}

}
