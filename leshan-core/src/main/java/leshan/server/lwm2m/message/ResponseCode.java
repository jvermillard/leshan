package leshan.server.lwm2m.message;

import org.apache.commons.lang.Validate;

import ch.ethz.inf.vs.californium.coap.CoAP;

/**
 * Response codes defined for LWM2M enabler
 */
public enum ResponseCode {

    CREATED, DELETED, CHANGED, CONTENT, UNAUTHORIZED, BAD_REQUEST, METHOD_NOT_ALLOWED, CONFLICT, NOT_FOUND;

    public static ResponseCode fromCoapCode(CoAP.ResponseCode code) {
        Validate.notNull(code);

        switch (code) {
        case CREATED:
            return CREATED;
        case DELETED:
            return DELETED;
        case CHANGED:
            return CHANGED;
        case CONTENT:
            return CONTENT;
        case BAD_REQUEST:
            return BAD_REQUEST;
        case UNAUTHORIZED:
            return UNAUTHORIZED;
        case NOT_FOUND:
            return NOT_FOUND;
        case METHOD_NOT_ALLOWED:
            return METHOD_NOT_ALLOWED;
        default:
            throw new IllegalArgumentException("Invalid CoAP code for LWM2M response: " + code);
        }
    }

}
