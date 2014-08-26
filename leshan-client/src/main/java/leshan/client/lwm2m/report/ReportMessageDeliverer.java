package leshan.client.lwm2m.report;

import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.InterfaceTypes;
import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.OperationTypes;
import leshan.client.lwm2m.response.OperationResponseCode;
import leshan.server.lwm2m.exception.InvalidUriException;
import leshan.server.lwm2m.message.ResourceSpec;
import ch.ethz.inf.vs.californium.coap.CoAP.Code;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.CoAP.Type;
import ch.ethz.inf.vs.californium.coap.OptionSet;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Exchange;
import ch.ethz.inf.vs.californium.server.MessageDeliverer;

public class ReportMessageDeliverer implements MessageDeliverer {

	private final ReportDownlink downlink;

	public ReportMessageDeliverer(final ReportDownlink downlink) {
		this.downlink = downlink;
	}

	@Override
	public void deliverRequest(final Exchange exchange) {
		final Request request = exchange.getRequest();

		final Response response;
		if(isObserveRequest(request)) {
			response = deliverObserveRequest(exchange);
		} else {
			response = null;
		}

		exchange.sendResponse(response);

	}

	private static boolean isObserveRequest(final Request request) {
		final Code requestCode = request.getCode();
		final OptionSet options = request.getOptions();

		return requestCode == Code.GET && options.hasObserve();
	}

	private static boolean isCancelObserveRequest(final Response response) {
		return response.getType()  == Type.RST;
	}

	private Response deliverObserveRequest(final Exchange exchange) {
		try {
			final ResourceSpec lwm2mUri = ResourceSpec.of(exchange.getRequest().getURI());
			downlink.observe(lwm2mUri.getObjectId(), lwm2mUri.getObjectInstanceId(), lwm2mUri.getResourceId());

			final Response response = new Response(ResponseCode.CONTENT);
			response.setPayload(OperationResponseCode.generateReasonPhrase(OperationResponseCode.valueOf(response.getCode().value), InterfaceTypes.REPORTING, OperationTypes.OBSERVE));
			return response;
		} catch (final InvalidUriException e) {
			final Response response = new Response(ResponseCode.NOT_FOUND);
			response.setPayload(OperationResponseCode.generateReasonPhrase(OperationResponseCode.valueOf(response.getCode().value), InterfaceTypes.REPORTING, OperationTypes.OBSERVE));
			return response;
		} catch (final Exception e) {
			// TODO: this should do permissions check for the server in the ACL
			return new Response(ResponseCode.METHOD_NOT_ALLOWED);
		}
	}

	@Override
	public void deliverResponse(final Exchange exchange, final Response response) {
		if(!isCancelObserveRequest(response)) {
			throw new UnsupportedOperationException("Cannot deliver response from ReportMessageDeliverer");
		}

		exchange.sendResponse(response);
	}

}
