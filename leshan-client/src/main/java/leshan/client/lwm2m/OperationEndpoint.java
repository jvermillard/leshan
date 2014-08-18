package leshan.client.lwm2m;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class OperationEndpoint {
	private final String objectId;
	private final String objectInstanceId;
	private final String resourceId;
	private final Map<String, String> queryString;
	
	public OperationEndpoint(String objectId, String objectInstanceId, final String resourceId, final Map<String, String> queryString ) {
		this.objectId = objectId;
		this.objectInstanceId = objectInstanceId;
		this.resourceId = resourceId;
		this.queryString = queryString;
	}
	
	@Override
	public String toString() {
		return "/" + 
				(StringUtils.isEmpty(objectId) ? 			"" : (objectId + "/")) +
				(StringUtils.isEmpty(objectInstanceId) ? 	"" : (objectInstanceId + "/")) +
				(StringUtils.isEmpty(resourceId) ? 			"" : (resourceId + "/")) +
				Request.toQueryStringMap(queryString);
	}
	
}