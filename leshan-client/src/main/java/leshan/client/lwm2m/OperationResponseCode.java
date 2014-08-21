package leshan.client.lwm2m;

import leshan.client.lwm2m.BootstrapMessageDeliverer.InterfaceTypes;
import leshan.client.lwm2m.BootstrapMessageDeliverer.OperationTypes;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

// TODO: Rename me to ResponseCode after CaliforniumResponseCode is purged from client visible layer
public enum OperationResponseCode {	
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
	private OperationResponseCode(final ResponseCode responseCode) {
		this.responseCode = responseCode;
	}
	
	public int getValue() {
		return this.responseCode.value;
	}
	
	@Override
	public String toString() {
		return this.responseCode.toString();
	}
	
	/**
	 * Converts the specified integer value to a response code.
	 *
	 * @param value the value
	 * @return the response code
	 * @throws IllegalArgumentException if integer value is not recognized
	 */
	public static OperationResponseCode valueOf(final int value) {
		final ResponseCode responseCode = ResponseCode.valueOf(value);
		
		for (final OperationResponseCode code : OperationResponseCode.values()) {
			if (code.responseCode == responseCode) {
				return code;
			}
		}
		
		throw new IllegalArgumentException("Unknown Leshan response code " + value);
	}

	public static boolean isSuccess(final OperationResponseCode code) {
		return ResponseCode.isSuccess(ResponseCode.valueOf(code.responseCode.value));
	}
	
	public static boolean isClientError(final OperationResponseCode code) {
		return ResponseCode.isClientError(ResponseCode.valueOf(code.responseCode.value));
	}
	
	public static boolean isServerError(final OperationResponseCode code) {
		return ResponseCode.isServerError(ResponseCode.valueOf(code.responseCode.value));
	}

	public static String generateReasonPhrase(final OperationResponseCode code, final InterfaceTypes interfaceType, final OperationTypes operationType) {
		if(interfaceType == InterfaceTypes.BOOTSTRAP) {
			if(operationType == OperationTypes.WRITE) {
				switch(code) {
					case CHANGED: 		return "\"Write\" operation is completed successfully";
					case BAD_REQUEST: 	return "￼The format of data to be written is different";
					default: 			throwError(code, interfaceType, operationType);
				}
			} else if(operationType == OperationTypes.REQUEST) {
				switch(code){
					case CHANGED: 		return "Request Bootstrap is completed successfully";
					case BAD_REQUEST: 	return "Unknown Endpoint Client Name";
					default: 			throwError(code, interfaceType, operationType);
				}
			} else if(operationType == OperationTypes.DELETE) {
				switch(code){
					case DELETED: 				return "\"Delete\" operation is completed successfully";
					case METHOD_NOT_ALLOWED:	return "Target is not allowed for \"Delete\" operation";
					default: 					throwError(code, interfaceType, operationType);
				}
			}
		} else if(interfaceType == InterfaceTypes.REGISTRATION){
			if(operationType == OperationTypes.REGISTER) {
				switch(code) {
					case CHANGED: 		return "\"Register\" operation is completed successfully";
					case BAD_REQUEST: 	return "The mandatory parameter is not specified or unknown parameter is specified";
					default: 			throwError(code, interfaceType, operationType);
				}
			}
		}
		
		throwError(code, interfaceType, operationType);
		//TODO remove this in the future
		return null;
	}
	
	private static void throwError(final OperationResponseCode code, final InterfaceTypes interfaceType, final OperationTypes operationType) {
		throw new IllegalArgumentException("Unsupported response for " + code + "; " + interfaceType + "; " + operationType);
	}
}