package leshan.client.lwm2m.util;

import java.util.List;
import java.util.Map;
import leshan.server.lwm2m.client.LinkObject;
import ch.ethz.inf.vs.californium.coap.LinkFormat;

public class LinkFormatUtils {
	public static final String INVALID_LINK_PAYLOAD = "<>";
	
	private static final String TRAILER = ", ";

	public static String payloadize(final LinkObject... linkObjects) {
		try{
		final StringBuilder builder = new StringBuilder();
		for(final LinkObject link : linkObjects){
			builder.append(payloadizeLink(link)).append(TRAILER);
		}
		
		builder.delete(builder.length() - TRAILER.length(), builder.length());
		
		return builder.toString();
		}
		catch(final Exception e){
			return INVALID_LINK_PAYLOAD;
		}
	}

	private static String payloadizeLink(final LinkObject link) {
		final StringBuilder builder = new StringBuilder();
		builder.append('<');
		builder.append(link.getUrl());
		builder.append('>');
		
		final Map<String, Object> attributes = link.getAttributes();
		
		if(hasPayloadAttributes(attributes)){
			builder.append(";");
			if (attributes.containsKey(LinkFormat.RESOURCE_TYPE)) {
				builder.append(LinkFormat.RESOURCE_TYPE).append("=\"").append(attributes.get(LinkFormat.RESOURCE_TYPE)).append("\"");
			}
			if (attributes.containsKey(LinkFormat.INTERFACE_DESCRIPTION)) {
				builder.append(LinkFormat.INTERFACE_DESCRIPTION).append("=\"").append(attributes.get(LinkFormat.INTERFACE_DESCRIPTION)).append("\"");
			}
			if (attributes.containsKey(LinkFormat.CONTENT_TYPE)) {
				builder.append(LinkFormat.CONTENT_TYPE).append("=\"").append(attributes.get(LinkFormat.CONTENT_TYPE)).append("\"");
			}
			if (attributes.containsKey(LinkFormat.MAX_SIZE_ESTIMATE)) {
				builder.append(LinkFormat.MAX_SIZE_ESTIMATE).append("=\"").append(attributes.get(LinkFormat.MAX_SIZE_ESTIMATE)).append("\"");
			}
			if (attributes.containsKey(LinkFormat.OBSERVABLE)) {
				builder.append(LinkFormat.OBSERVABLE);
			}
			
		}
		
		return builder.toString();
	}

	private static String listToLinkString(final List<String> attributeValue) {
		return attributeValue.toString().replaceAll("[\\[\\]]+", "\"");
	}

	private static boolean hasPayloadAttributes(final Map<String, Object> attributes) {
		if(attributes.containsKey(LinkFormat.RESOURCE_TYPE) ||
			attributes.containsKey(LinkFormat.INTERFACE_DESCRIPTION) ||
			attributes.containsKey(LinkFormat.CONTENT_TYPE) ||
			attributes.containsKey(LinkFormat.MAX_SIZE_ESTIMATE) ||
			attributes.containsKey(LinkFormat.OBSERVABLE)){
				return true;
			}
			
			return false;
	}

}
