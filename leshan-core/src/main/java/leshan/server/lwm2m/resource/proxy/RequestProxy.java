package leshan.server.lwm2m.resource.proxy;

import java.util.List;

public abstract class RequestProxy {

	public abstract boolean isConfirmable();

	public abstract List<String> getURIQueries();

	public abstract boolean hasPayload();

	public abstract byte[] getPayload();

	public abstract ResponseProxy sendAndWaitForResponse(int timeoutMilli);

}
