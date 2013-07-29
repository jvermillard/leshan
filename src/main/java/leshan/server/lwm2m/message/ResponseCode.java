package leshan.server.lwm2m.message;

/**
 * The code of the CoAP response
 */
public enum ResponseCode {

    /** 2.01 */
    CREATED(65),

    /** 2.02 */
    DELETED(66),

    /** 4.00 */
    BAD_REQUEST(128),

    /** 4.09 */
    CONFLICT(137);

    /**
     * The CoAP code is an 8-bit code value (3 bits for the code class and 5 bits for the detail).
     * <p>
     * E.g. 2.01 -> 010-00001 = decimal 65
     * </p>
     */
    private final int coapCode;

    private ResponseCode(int coapCode) {
        this.coapCode = coapCode;
    }

    public int getCoapCode() {
        return coapCode;
    }

}
