package leshan.server.lwm2m.message.client;

import leshan.server.lwm2m.message.ResponseCode;

import org.apache.commons.lang.Validate;
import org.apache.mina.filter.query.Response;

/**
 * A response to a server request.
 */
public class ClientResponse implements Response {

    protected final int id;

    protected final ResponseCode code;

    public ClientResponse(int id, ResponseCode code) {
        Validate.notNull(code);

        this.id = id;
        this.code = code;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ClientResponse [id=").append(id).append(", code=").append(code).append("]");
        return builder.toString();
    }

}
