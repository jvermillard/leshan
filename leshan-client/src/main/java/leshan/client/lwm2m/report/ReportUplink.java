package leshan.client.lwm2m.report;

import java.net.InetSocketAddress;

import leshan.client.lwm2m.Uplink;
import leshan.client.lwm2m.response.Callback;

import org.apache.commons.lang.Validate;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;

public class ReportUplink extends Uplink {
	private static final String ENDPOINT = "ep";

	public ReportUplink(final InetSocketAddress destination, final CoAPEndpoint endpoint) {
		super(destination, endpoint);
	}

	public void notify(final byte[] token, final int messageId, final byte[] newValue, final Callback callback) {
		Validate.notNull(token);
		Validate.notNull(newValue);
		Validate.notNull(callback);

		final Response response = createNewNotifyResponse(token, newValue, messageId);

//		sendAsyncRequest(callback, request);
	}

	private Response createNewNotifyResponse(final byte[] token, final byte[] payload, final int messageId) {
		final Response response = new Response(ResponseCode.CHANGED);
		response.setToken(token);
		response.setPayload(payload);
		response.setMID(messageId);

		return response;
	}

}
