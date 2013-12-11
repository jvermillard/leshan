package leshan.server.lwm2m.message;

import org.apache.commons.lang.Validate;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

/**
 * A response to a server request.
 */
public class ClientResponse {

    protected final ResponseCode code;

    public ClientResponse(ResponseCode code) {
        Validate.notNull(code);

        this.code = code;
    }

    public ResponseCode getCode() {
        return code;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ClientResponse [code=").append(code).append("]");
        return builder.toString();
    }

}
