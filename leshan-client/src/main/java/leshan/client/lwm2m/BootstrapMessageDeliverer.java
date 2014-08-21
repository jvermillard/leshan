package leshan.client.lwm2m;

import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.server.lwm2m.exception.InvalidUriException;
import leshan.server.lwm2m.message.ResourceSpec;
import ch.ethz.inf.vs.californium.coap.CoAP.Code;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Exchange;
import ch.ethz.inf.vs.californium.server.MessageDeliverer;

public class BootstrapMessageDeliverer implements MessageDeliverer {

	public enum InterfaceTypes {
		BOOTSTRAP,
		REGISTRATION,
		MANAGEMENT,
		REPORTING;
	}

	public enum OperationTypes {
		CREATE,
		DELETE,
		DEREGISTER,
		DISCOVER,
		EXECUTE,
		NOTIFY,
		OBSERVE,
		READ,
		REQUEST,
		REGISTER,
		UPDATE,
		WRITE,
		WRITE_ATTRIBUTES;
	}

	private final BootstrapDownlink downlink;

	public BootstrapMessageDeliverer(final BootstrapDownlink downlink) {
		this.downlink = downlink;
	}

	@Override
	public void deliverRequest(final Exchange exchange) {
		final Code requestCode = exchange.getRequest().getCode();
		
		final Response response;
		if(requestCode == Code.DELETE) {
			response = deliverDeleteRequest(exchange);
		} else if(requestCode == Code.PUT) {
			response = deliverWriteRequest(exchange);
		} else {
			response = null;
		}
		
		exchange.sendResponse(response);

	}

	private Response deliverWriteRequest(final Exchange exchange) {
		try {
			final ResourceSpec lwm2mUri = ResourceSpec.of(exchange.getRequest().getURI());
			downlink.write(lwm2mUri.getObjectId(), lwm2mUri.getObjectInstanceId(), lwm2mUri.getResourceId());

			Response response = new Response(ResponseCode.CHANGED);
			response.setPayload(OperationResponseCode.generateReasonPhrase(OperationResponseCode.valueOf(response.getCode().value), InterfaceTypes.BOOTSTRAP, OperationTypes.WRITE));
			return response;
		} catch (final InvalidUriException e) {
			Response response = new Response(ResponseCode.BAD_REQUEST);
			response.setPayload(OperationResponseCode.generateReasonPhrase(OperationResponseCode.valueOf(response.getCode().value), InterfaceTypes.BOOTSTRAP, OperationTypes.WRITE));
			return response;
		} catch (final Exception e) {
			return new Response(ResponseCode.INTERNAL_SERVER_ERROR);
		}
	}

	private Response deliverDeleteRequest(final Exchange exchange) {
		try {
			final ResourceSpec lwm2mUri = ResourceSpec.of(exchange.getRequest().getURI());
			downlink.delete(lwm2mUri.getObjectId(), lwm2mUri.getObjectInstanceId());

			Response response = new Response(ResponseCode.DELETED);
			response.setPayload(OperationResponseCode.generateReasonPhrase(OperationResponseCode.valueOf(response.getCode().value), InterfaceTypes.BOOTSTRAP, OperationTypes.DELETE));
			return response;
		} catch (final InvalidUriException e) {
			Response response = new Response(ResponseCode.METHOD_NOT_ALLOWED);
			response.setPayload(OperationResponseCode.generateReasonPhrase(OperationResponseCode.valueOf(response.getCode().value), InterfaceTypes.BOOTSTRAP, OperationTypes.DELETE));
			return response;
		} catch (final Exception e) {
			return new Response(ResponseCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void deliverResponse(final Exchange exchange, final Response response) {
		// TODO: DOES NOTHING????
		throw new UnsupportedOperationException("Cannot deliver response from BootstrapMessageDeliverer");
	}

}
