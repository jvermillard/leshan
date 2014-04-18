package leshan.server.lwm2m.message;


/**
 * Response codes defined for LWM2M enabler
 */
public enum ResponseCode {

    CREATED(65), DELETED(66), CHANGED(68), CONTENT(69), UNAUTHORIZED(129), BAD_REQUEST(128), METHOD_NOT_ALLOWED(133),
    // CONFLICT,
    NOT_FOUND(132);

    private int code;

    private ResponseCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static ResponseCode fromCoapCode(int code) {

        for (ResponseCode responseCode : values()) {
            if (responseCode.code == code) {
                return responseCode;
            }
        }

        throw new IllegalArgumentException("Invalid CoAP code for LWM2M response: " + code);
    }

}
