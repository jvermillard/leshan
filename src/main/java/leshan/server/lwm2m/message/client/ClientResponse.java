package leshan.server.lwm2m.message.client;

import leshan.server.lwm2m.message.ResponseCode;

import org.apache.commons.lang.Validate;
import org.apache.mina.filter.query.Response;

/**
 * A response from the LW-M2M client.
 */
public class ClientResponse implements Response {

    private final int id;

    private final ResponseCode code;

    private final byte[] content;

    private final String contentFormat;

    public ClientResponse(int id, ResponseCode code, byte[] content, String contentFormat) {
        Validate.notNull(code);

        this.id = id;
        this.code = code;
        this.content = content;
        this.contentFormat = contentFormat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object requestId() {
        return id;
    }

    public ResponseCode getCode() {
        return code;
    }

    public byte[] getContent() {
        return content;
    }

    public String getContentFormat() {
        return contentFormat;
    }

}
