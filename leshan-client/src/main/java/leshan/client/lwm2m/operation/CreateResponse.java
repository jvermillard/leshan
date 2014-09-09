package leshan.client.lwm2m.operation;

import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.BAD_REQUEST;
import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CREATED;
import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.METHOD_NOT_ALLOWED;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

import com.google.common.base.Objects;

public class CreateResponse extends BaseLwM2mResponse {

	private final String location;

	private CreateResponse(final ResponseCode code, final String location) {
		super(code, new byte[0]);
		this.location = location;
	}

	private CreateResponse(final ResponseCode code) {
		this(code, null);
	}

	public static CreateResponse success(final int instanceId) {
		return new CreateResponse(CREATED, Integer.toString(instanceId));
	}

	public static CreateResponse methodNotAllowed() {
		return new CreateResponse(METHOD_NOT_ALLOWED);
	}

	public static CreateResponse invalidResource() {
		return new CreateResponse(BAD_REQUEST);
	}

	public String getLocation() {
		return location;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof CreateResponse) || !super.equals(o)) {
			return false;
		}
		final CreateResponse other = (CreateResponse)o;
		return Objects.equal(location, other.location);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), location);
	}

}
