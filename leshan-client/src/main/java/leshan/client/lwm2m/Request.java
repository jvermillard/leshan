package leshan.client.lwm2m;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class Request {
//	private ch.ethz.inf.vs.californium.coap.Request coapRequest;

	private class OperationEndpoint {
		private String objectId;
		private String objectInstanceId;
		private String resourceId;
		
		private Map<String, String> queryString;
		
		@Override
		public String toString() {
			return "/" + 
					(StringUtils.isEmpty(objectId) ? 			"" : (objectId +"/")) +
					(StringUtils.isEmpty(objectInstanceId) ? 	"" : (objectInstanceId +"/")) +
					(StringUtils.isEmpty(resourceId) ? 			"" : (resourceId +"/")) +
					toQueryStringMap(queryString);
		}
		
	}
	
	private class BootstrapEndpoint {
		private Map<String, String> queryString;
		
		public BootstrapEndpoint(final Map<String, String> queryString) {
			this.queryString = new HashMap<>(queryString);
		}
		
		@Override
		public String toString() {
			return "/bs?" + toQueryStringMap(queryString);
		}
	}
	
	private class RegistrationEndpoint {
		private Map<String, String> queryString;
		
		public RegistrationEndpoint(final Map<String, String> queryString) {
			this.queryString = new HashMap<>(queryString);
		}
		
		@Override
		public String toString() {
			return "/rd?" + toQueryStringMap(queryString);
		}
	}
	
	private class Destination {
		private String host;
		private String port;
	}
	
	private static String toQueryStringMap(Map<String, String> map) {
		if(map == null) {
			throw new IllegalArgumentException("Map was null!");
		}
		
		if(map.isEmpty()) {
			return "";
		}
		
		StringBuilder builder = new StringBuilder();
		for(Map.Entry<String, String> entry : map.entrySet()) {
			builder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		builder.deleteCharAt(builder.length() - 1);
		return builder.toString();
	}
}
