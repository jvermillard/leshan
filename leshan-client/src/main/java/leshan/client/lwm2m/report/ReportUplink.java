package leshan.client.lwm2m.report;

import java.net.InetSocketAddress;

import leshan.client.lwm2m.Uplink;
import leshan.client.lwm2m.response.Callback;

import org.apache.commons.lang.Validate;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;
import ch.ethz.inf.vs.californium.network.Exchange;

public class ReportUplink extends Uplink {

	public ReportUplink(final InetSocketAddress destination, final CoAPEndpoint endpoint) {
		super(destination, endpoint);
	}

	public void notify(final byte[] token, final byte[] newValue, final Callback callback) {
		Validate.notNull(token);
		Validate.notNull(newValue);
		Validate.notNull(callback);

		final Response response = createNewNotifyResponse(token, newValue);
		final Exchange exchange = Observations.INSTANCE.getExchangesForToken(token);
		if(exchange == null) {
			throw new IllegalArgumentException("Token " + token + " does is not being observed.");
		}
		sendAsyncResponse(exchange, response, callback);
	}

	private Response createNewNotifyResponse(final byte[] token, final byte[] payload) {
		final Response response = new Response(ResponseCode.CHANGED);
		response.setToken(token);
		response.setPayload(payload);

		return response;
	}

}
