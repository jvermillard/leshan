package leshan.server.lwm2m.message;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

/**
 * A response with content from the LW-M2M client.
 */
public class ContentResponse extends ClientResponse {

    private final byte[] content;

    private final ContentFormat format;

    public ContentResponse(byte[] content, ContentFormat format) {
        super(ResponseCode.CONTENT);

        Validate.notNull(format);

        this.content = content;
        this.format = format;
    }

    public byte[] getContent() {
        return content;
    }

    public ContentFormat getFormat() {
        return format;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ContentResponse [content=").append(Arrays.toString(content)).append(", format=").append(format)
                .append("]");
        return builder.toString();
    }

}
