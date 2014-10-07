package leshan.client.lwm2m.response;

import static leshan.client.lwm2m.response.OperationResponseCode.BAD_REQUEST;
import static leshan.client.lwm2m.response.OperationResponseCode.CREATED;
import static leshan.client.lwm2m.response.OperationResponseCode.METHOD_NOT_ALLOWED;

import java.util.Objects;


public class CreateResponse extends BaseLwM2mResponse {

	private final String location;

	private CreateResponse(final OperationResponseCode code, final String location) {
		super(code, new byte[0]);
		this.location = location;
	}

	private CreateResponse(final OperationResponseCode code) {
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
		return Objects.equals(location, other.location);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), location);
	}

}
