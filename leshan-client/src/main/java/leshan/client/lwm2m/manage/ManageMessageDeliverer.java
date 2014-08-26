package leshan.client.lwm2m.manage;

import java.util.List;

import leshan.client.lwm2m.response.OperationResponse;
import leshan.server.lwm2m.exception.InvalidUriException;
import leshan.server.lwm2m.message.ResourceSpec;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.coap.OptionSet;
import ch.ethz.inf.vs.californium.coap.Request;
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
			final Request request = exchange.getRequest();
			final String uri = request.getURI();
			final ResourceSpec spec = ResourceSpec.of(uri);

			final OperationResponse opResponse = getResponseFromDownlink(spec, request);
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

	private OperationResponse getResponseFromDownlink(final ResourceSpec spec, final Request request) {
		final byte[] payload = request.getPayload();
		switch (request.getCode()) {
		case GET: return getFromDownlink(spec, request.getOptions());
		case PUT: return putToDownlink(spec, payload, request.getOptions());
		case POST: return postToDownlink(spec, payload);
		case DELETE: return deleteFromDownlink(spec);
		default: return null;
		}
	}

	private OperationResponse getFromDownlink(final ResourceSpec spec, final OptionSet optionSet) {
		if (optionSet.hasAccept() && optionSet.getAccept() == MediaTypeRegistry.APPLICATION_LINK_FORMAT) {
			return discover(spec);
		}
		return read(spec);
	}

	private OperationResponse read(final ResourceSpec spec) {
		if (spec.getObjectInstanceId() == ResourceSpec.DOES_NOT_EXIST) {
			return downlink.read(spec.getObjectId());
		} else if (spec.getResourceId() == ResourceSpec.DOES_NOT_EXIST) {
			return downlink.read(spec.getObjectId(), spec.getObjectInstanceId());
		} else {
			return downlink.read(spec.getObjectId(), spec.getObjectInstanceId(), spec.getResourceId());
		}
	}

	private OperationResponse discover(final ResourceSpec spec) {
		if (spec.getObjectInstanceId() == ResourceSpec.DOES_NOT_EXIST) {
			return downlink.discover(spec.getObjectId());
		} else if (spec.getResourceId() == ResourceSpec.DOES_NOT_EXIST) {
			return downlink.discover(spec.getObjectId(), spec.getObjectInstanceId());
		} else {
			return downlink.discover(spec.getObjectId(), spec.getObjectInstanceId(), spec.getResourceId());
		}
	}

	private OperationResponse putToDownlink(final ResourceSpec spec, final byte[] payload, final OptionSet optionSet) {
		if (optionSet.getURIQueryCount() > 0) {
			return writeAttributes(spec, optionSet.getURIQueries());
		}
		return replace(spec, payload);
	}

	private OperationResponse writeAttributes(final ResourceSpec spec, final List<String> queries) {
		if (spec.getObjectInstanceId() == ResourceSpec.DOES_NOT_EXIST) {
			return downlink.writeAttributes(spec.getObjectId(), queries);
		} else if (spec.getResourceId() == ResourceSpec.DOES_NOT_EXIST) {
			return downlink.writeAttributes(spec.getObjectId(), spec.getObjectInstanceId(),
					queries);
		} else {
			return downlink.writeAttributes(spec.getObjectId(), spec.getObjectInstanceId(),
					spec.getResourceId(), queries);
		}
	}

	private OperationResponse replace(final ResourceSpec spec, final byte[] payload) {
		if (spec.getResourceId() != ResourceSpec.DOES_NOT_EXIST) {
			return downlink.replace(spec.getObjectId(), spec.getObjectInstanceId(), spec.getResourceId(), new String(payload));
		} else if (spec.getObjectInstanceId() != ResourceSpec.DOES_NOT_EXIST){
			return downlink.replace(spec.getObjectId(), spec.getObjectInstanceId(), new String(payload));
		} else {
			return disallow("Write");
		}
	}

	private OperationResponse postToDownlink(final ResourceSpec spec, final byte[] payload) {
		if (spec.getResourceId() != ResourceSpec.DOES_NOT_EXIST) {
			return downlink.partialUpdateOrExecute(spec.getObjectId(), spec.getObjectInstanceId(), spec.getResourceId(), new String(payload));
		} else if (spec.getObjectInstanceId() != ResourceSpec.DOES_NOT_EXIST) {
			return downlink.partialUpdateOrCreate(spec.getObjectId(), spec.getObjectInstanceId(), new String(payload));
		} else {
			return downlink.create(spec.getObjectId(), new String(payload));
		}
	}

	private OperationResponse deleteFromDownlink(final ResourceSpec spec) {
		if (spec.getResourceId() == ResourceSpec.DOES_NOT_EXIST &&
				spec.getObjectInstanceId() != ResourceSpec.DOES_NOT_EXIST) {
			return downlink.delete(spec.getObjectId(), spec.getObjectInstanceId());
		} else {
			return disallow("Delete");
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

	private OperationResponse disallow(final String method) {
		final Response response = new Response(ResponseCode.METHOD_NOT_ALLOWED);
		response.setPayload("Target is not allowed for \"" + method + "\" operation");
		return OperationResponse.of(response);
	}

	@Override
	public void deliverResponse(final Exchange exchange, final Response response) {
		// TODO Auto-generated method stub

	}

}
