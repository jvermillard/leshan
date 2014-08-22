package leshan.client.lwm2m.util;

import java.util.List;
import java.util.Set;

import ch.ethz.inf.vs.californium.WebLink;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.server.resources.ResourceAttributes;

public class LinkFormatUtils {
	public static final String INVALID_LINK_PAYLOAD = null;
	
	private static final String TRAILER = ", ";

	public static String payloadize(final Set<WebLink> links) {
		try{
		final StringBuilder builder = new StringBuilder();
		for(final WebLink link : links){
			builder.append(payloadize(link)).append(TRAILER);
		}
		
		builder.delete(builder.length() - TRAILER.length(), builder.length());
		
		return builder.toString();
		}
		catch(final Exception e){
			return INVALID_LINK_PAYLOAD;
		}
	}

	private static String payloadize(final WebLink link) {
		final StringBuilder builder = new StringBuilder();
		builder.append('<');
		builder.append(link.getURI());
		builder.append('>');
		
		final ResourceAttributes attributes = link.getAttributes();
		
		if(hasPayloadAttributes(attributes)){
			builder.append(";");
			if (attributes.containsAttribute(LinkFormat.RESOURCE_TYPE)) {
				builder.append(LinkFormat.RESOURCE_TYPE).append("=").append(listToLinkString(attributes.getResourceTypes()));
			}
			if (attributes.containsAttribute(LinkFormat.INTERFACE_DESCRIPTION)) {
				builder.append(LinkFormat.INTERFACE_DESCRIPTION).append("=").append(listToLinkString(attributes.getInterfaceDescriptions()));
			}
			if (attributes.containsAttribute(LinkFormat.CONTENT_TYPE)) {
				builder.append(LinkFormat.CONTENT_TYPE).append("=").append(listToLinkString(attributes.getContentTypes()));
			}
			if (attributes.containsAttribute(LinkFormat.MAX_SIZE_ESTIMATE)) {
				builder.append(LinkFormat.MAX_SIZE_ESTIMATE).append("=").append(attributes.getMaximumSizeEstimate());
			}
			if (attributes.hasObservable()) {
				builder.append(LinkFormat.OBSERVABLE);
			}
			
		}
		
		return builder.toString();
	}

	private static String listToLinkString(final List<String> attributeValue) {
		return attributeValue.toString().replaceAll("[\\[\\]]+", "\"");
	}

	private static boolean hasPayloadAttributes(final ResourceAttributes attributes) {
		if(attributes.containsAttribute(LinkFormat.RESOURCE_TYPE) ||
			attributes.containsAttribute(LinkFormat.INTERFACE_DESCRIPTION) ||
			attributes.containsAttribute(LinkFormat.CONTENT_TYPE) ||
			attributes.containsAttribute(LinkFormat.MAX_SIZE_ESTIMATE) ||
			attributes.hasObservable()){
				return true;
			}
			
			return false;
	}

}
