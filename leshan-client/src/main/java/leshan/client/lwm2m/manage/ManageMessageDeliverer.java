package leshan.client.lwm2m.manage;

import leshan.client.lwm2m.response.OperationResponse;
import leshan.server.lwm2m.exception.InvalidUriException;
import leshan.server.lwm2m.message.ResourceSpec;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Exchange;
import ch.ethz.inf.vs.californium.server.MessageDeliverer;

public class ManageMessageDeliverer implements MessageDeliverer {

	private final ManageDownlink downlink;

	public ManageMessageDeliverer(final ManageDownlink downlink) {
		this.downlink = downlink;
	}

	@Override
	public void deliverRequest(final Exchange exchange) {
		try {
			final String uri = exchange.getRequest().getURI();
			final ResourceSpec spec = ResourceSpec.of(uri);

			final OperationResponse opResponse = getResponseFromDownlink(spec);
			if (opResponse == null) {
				sendResponse(exchange, ResponseCode.INTERNAL_SERVER_ERROR, uri + " was null");
			}
			sendResponse(exchange, opResponse.getResponseCode(), opResponse.getPayload());
		} catch (final InvalidUriException e) {
			sendResponse(exchange, ResponseCode.BAD_REQUEST, "Invalid URI");
		} catch (final Exception e) {
			sendResponse(exchange, ResponseCode.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	private OperationResponse getResponseFromDownlink(final ResourceSpec spec) {
		if (spec.getObjectInstanceId() == ResourceSpec.DOES_NOT_EXIST) {
			return downlink.read(spec.getObjectId());
		} else if (spec.getResourceId() == ResourceSpec.DOES_NOT_EXIST) {
			return downlink.read(spec.getObjectId(), spec.getObjectInstanceId());
		} else {
			return downlink.read(spec.getObjectId(), spec.getObjectInstanceId(), spec.getResourceId());
		}
	}

	private void sendResponse(final Exchange exchange, final ResponseCode code, final byte[] payload) {
		final Response response = new Response(code);
		response.setPayload(payload);
		exchange.sendResponse(response);
	}

	private void sendResponse(final Exchange exchange, final ResponseCode code, final String payload) {
		sendResponse(exchange, code, payload == null ? new byte[0] : payload.getBytes());
	}

	@Override
	public void deliverResponse(final Exchange exchange, final Response response) {
		// TODO Auto-generated method stub

	}

}
