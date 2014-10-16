package leshan.connector.californium.resource;

import java.util.List;

import leshan.connector.californium.server.CaliforniumResponseCode;
import leshan.server.lwm2m.request.CoapResponseCode.ResponseCode;
import leshan.server.lwm2m.resource.proxy.RequestProxy;
import leshan.server.lwm2m.resource.proxy.ResponseProxy;

import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.Endpoint;

public class CaliforniumRequestProxy extends RequestProxy {
	private static final CaliforniumResponseCode CALIFORNIUM_RESPONSE_CODE = new CaliforniumResponseCode();

	private final Request request;
	private Endpoint endpoint;

	public CaliforniumRequestProxy(final Request request) {
		this.request = request;
	}

	public CaliforniumRequestProxy(final Request request, final Endpoint e) {
		this(request);
		this.endpoint = e;
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

	@Override
	public ResponseProxy sendAndWaitForResponse(final int timeoutMilli) {
        Response response;
		try {
			response = request.send(endpoint).waitForResponse(timeoutMilli);
			if(response == null){
				return ResponseProxy.failure("Timeout", ResponseCode.NOT_FOUND);
			}
			else{
				return new CaliforniumResponseProxy(response, CALIFORNIUM_RESPONSE_CODE.fromCoapCode(response.getCode().ordinal()));
			}
		} catch (final InterruptedException e) {
			return ResponseProxy.failure(e.getLocalizedMessage(), ResponseCode.BAD_REQUEST);
		}
        
	}

}
