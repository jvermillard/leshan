package leshan.client.lwm2m;

import java.util.Arrays;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import ch.ethz.inf.vs.californium.coap.Response;

public class ResponseMatcher extends BaseMatcher<Response> {

	private final OperationResponseCode code;
	private final byte[] payload;

	public ResponseMatcher(final OperationResponseCode code, final byte[] payload) {
		this.code = code;
		this.payload = payload;
	}

	@Override
	public boolean matches(final Object arg0) {
		OperationResponseCode responseCode = OperationResponseCode.valueOf(((Response)arg0).getCode().value);		
		return responseCode == code && Arrays.equals(payload, ((Response)arg0).getPayload());
	}

	@Override
	public void describeTo(final Description arg0) {
	}

}