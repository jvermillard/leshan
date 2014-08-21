package leshan.client.lwm2m;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

public enum LeshanResponseCode {	
	// Success
	CREATED(65),
	DELETED(66),
	VALID(67),
	CHANGED(68),
	CONTENT(69),
	CONTINUE(95),

	// Client error
	BAD_REQUEST(128),
	UNAUTHORIZED(129),
	BAD_OPTION(130),
	FORBIDDEN(131),
	NOT_FOUND(132),
	METHOD_NOT_ALLOWED(133),
	NOT_ACCEPTABLE(134),
	REQUEST_ENTITY_INCOMPLETE(136),
	PRECONDITION_FAILED(140),
	REQUEST_ENTITY_TOO_LARGE(141), 
	UNSUPPORTED_CONTENT_FORMAT(143),

	// Server error
	INTERNAL_SERVER_ERROR(160),
	NOT_IMPLEMENTED(161),
	BAD_GATEWAY(162),
	SERVICE_UNAVAILABLE(163),
	GATEWAY_TIMEOUT(164),
	PROXY_NOT_SUPPORTED(165);
	
	/** The code value. */
	public final int value;
	
	/**
	 * Instantiates a new response code with the specified integer value.
	 *
	 * @param value the integer value
	 */
	private LeshanResponseCode(int value) {
		this.value = value;
	}
	
	/**
	 * Converts the specified integer value to a response code.
	 *
	 * @param value the value
	 * @return the response code
	 * @throws IllegalArgumentException if integer value is not recognized
	 */
	public static LeshanResponseCode valueOf(int value) {
		switch (value) {
			case 65: return CREATED;
			case 66: return DELETED;
			case 67: return VALID;
			case 68: return CHANGED;
			case 69: return CONTENT;
			case 128: return BAD_REQUEST;
			case 129: return UNAUTHORIZED;
			case 130: return BAD_OPTION;
			case 131: return FORBIDDEN;
			case 132: return NOT_FOUND;
			case 133: return METHOD_NOT_ALLOWED;
			case 134: return NOT_ACCEPTABLE;
			case 136: return REQUEST_ENTITY_INCOMPLETE;
			case 140: return PRECONDITION_FAILED;
			case 141: return REQUEST_ENTITY_TOO_LARGE;
			case 143: return UNSUPPORTED_CONTENT_FORMAT;
			case 160: return INTERNAL_SERVER_ERROR;
			case 161: return NOT_IMPLEMENTED;
			case 162: return BAD_GATEWAY;
			case 163: return SERVICE_UNAVAILABLE;
			case 164: return GATEWAY_TIMEOUT;
			case 165: return PROXY_NOT_SUPPORTED;
			default: // Make an extensive search
				for (LeshanResponseCode code : LeshanResponseCode.values()) {
					if (code.value == value) {
						return code;
					}
				}
				
				throw new IllegalArgumentException("Unknown CoAP response code " + value);
		}
	}
	
	@Override
	public String toString() {
		return ResponseCode.valueOf(this.value).toString();
	}
	
	public static boolean isSuccess(LeshanResponseCode code) {
		return ResponseCode.isSuccess(ResponseCode.valueOf(code.value));
	}
	
	public static boolean isClientError(LeshanResponseCode code) {
		return ResponseCode.isClientError(ResponseCode.valueOf(code.value));
	}
	
	public static boolean isServerError(LeshanResponseCode code) {
		return ResponseCode.isServerError(ResponseCode.valueOf(code.value));
	}
}