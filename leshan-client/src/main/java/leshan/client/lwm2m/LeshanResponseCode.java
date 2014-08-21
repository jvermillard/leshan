package leshan.client.lwm2m;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

public enum LeshanResponseCode {	
	// Success
	CREATED(ResponseCode.CREATED),
	DELETED(ResponseCode.DELETED),
	VALID(ResponseCode.VALID),
	CHANGED(ResponseCode.CHANGED),
	CONTENT(ResponseCode.CONTENT),
	CONTINUE(ResponseCode.CONTINUE),

	// Client error
	BAD_REQUEST(ResponseCode.BAD_REQUEST),
	UNAUTHORIZED(ResponseCode.UNAUTHORIZED),
	BAD_OPTION(ResponseCode.BAD_OPTION),
	FORBIDDEN(ResponseCode.FORBIDDEN),
	NOT_FOUND(ResponseCode.NOT_FOUND),
	METHOD_NOT_ALLOWED(ResponseCode.METHOD_NOT_ALLOWED),
	NOT_ACCEPTABLE(ResponseCode.NOT_ACCEPTABLE),
	REQUEST_ENTITY_INCOMPLETE(ResponseCode.REQUEST_ENTITY_INCOMPLETE),
	PRECONDITION_FAILED(ResponseCode.PRECONDITION_FAILED),
	REQUEST_ENTITY_TOO_LARGE(ResponseCode.REQUEST_ENTITY_TOO_LARGE), 
	UNSUPPORTED_CONTENT_FORMAT(ResponseCode.UNSUPPORTED_CONTENT_FORMAT),

	// Server error
	INTERNAL_SERVER_ERROR(ResponseCode.INTERNAL_SERVER_ERROR),
	NOT_IMPLEMENTED(ResponseCode.NOT_IMPLEMENTED),
	BAD_GATEWAY(ResponseCode.BAD_GATEWAY),
	SERVICE_UNAVAILABLE(ResponseCode.SERVICE_UNAVAILABLE),
	GATEWAY_TIMEOUT(ResponseCode.GATEWAY_TIMEOUT),
	PROXY_NOT_SUPPORTED(ResponseCode.PROXY_NOT_SUPPORTED);
	
	/** The code value. */
	private final ResponseCode responseCode;
	
	/**
	 * Instantiates a new response code with the specified integer value.
	 *
	 * @param value the integer value
	 */
	private LeshanResponseCode(ResponseCode responseCode) {
		this.responseCode = responseCode;
	}
	
	/**
	 * Converts the specified integer value to a response code.
	 *
	 * @param value the value
	 * @return the response code
	 * @throws IllegalArgumentException if integer value is not recognized
	 */
	public static LeshanResponseCode valueOf(int value) {
		ResponseCode responseCode = ResponseCode.valueOf(value);
		
		for (LeshanResponseCode code : LeshanResponseCode.values()) {
			if (code.responseCode == responseCode) {
				return code;
			}
		}
		
		throw new IllegalArgumentException("Unknown Leshan response code " + value);
	}
	
	@Override
	public String toString() {
		return this.responseCode.toString();
	}
	
	public static boolean isSuccess(LeshanResponseCode code) {
		return ResponseCode.isSuccess(ResponseCode.valueOf(code.responseCode.value));
	}
	
	public static boolean isClientError(LeshanResponseCode code) {
		return ResponseCode.isClientError(ResponseCode.valueOf(code.responseCode.value));
	}
	
	public static boolean isServerError(LeshanResponseCode code) {
		return ResponseCode.isServerError(ResponseCode.valueOf(code.responseCode.value));
	}
}