package leshan.client.request;

import java.util.Map;

public interface LwM2mContentRequest {

	public abstract Map<String, String> getClientParameters();

}