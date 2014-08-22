package leshan.client.lwm2m.request;

import java.util.Map;

public class Request {
//	private ch.ethz.inf.vs.californium.coap.Request coapRequest;

	public static String toQueryStringMap(Map<String, String> map) {
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
	
	public static ch.ethz.inf.vs.californium.coap.Request toCaliforniumRequest(Request r) {
		
		return null;
	}
}
