package leshan.server.lwm2m.message;

import org.apache.commons.lang.Validate;

import ch.ethz.inf.vs.californium.coap.CoAP;

/**
 * Response codes defined for LWM2M enabler
 */
public enum ResponseCode {

    /** Resource correctly created */
    CREATED,
    /** Resource correctly deleted */
    DELETED,
    /** Resource correctly changed */
    CHANGED,
    /** Content correctly delivered */
    CONTENT,
    /** Operation not authorized */
    UNAUTHORIZED,
    /** Cannot fulfill the request, it's incorrect */
    BAD_REQUEST,
    /** This method (GET/PUT/POST/DELETE) is not allowed on this resource */
    METHOD_NOT_ALLOWED,
    /** The End-point Client Name results in a duplicate entry on the LWM2M Server */
    CONFLICT,
    /** Resource not found */
    NOT_FOUND;

    public static ResponseCode fromCoapCode(int code) {
        Validate.notNull(code);

        if (code == CoAP.ResponseCode.CREATED.value) {
            return CREATED;
        } else if (code == CoAP.ResponseCode.DELETED.value) {
            return DELETED;
        } else if (code == CoAP.ResponseCode.CHANGED.value) {
            return CHANGED;
        } else if (code == CoAP.ResponseCode.CONTENT.value) {
            return CONTENT;
        } else if (code == CoAP.ResponseCode.BAD_REQUEST.value) {
            return BAD_REQUEST;
        } else if (code == CoAP.ResponseCode.UNAUTHORIZED.value) {
            return UNAUTHORIZED;
        } else if (code == CoAP.ResponseCode.NOT_FOUND.value) {
            return NOT_FOUND;
        } else if (code == CoAP.ResponseCode.METHOD_NOT_ALLOWED.value) {
            return METHOD_NOT_ALLOWED;
        } else if (code == 137) {
            return CONFLICT;
        } else {
            throw new IllegalArgumentException("Invalid CoAP code for LWM2M response: " + code);
        }
    }
}
