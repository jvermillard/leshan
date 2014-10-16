/*
 * Copyright (c) 2013, Sierra Wireless,
 * Copyright (c) 2014, Zebra Technologies,
 * 
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of {{ project }} nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package leshan.client.lwm2m.response;

import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.InterfaceTypes;
import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.OperationTypes;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;

// TODO: Rename me to ResponseCode after CaliforniumResponseCode is purged from client visible layer
// TODO: Remove Californium-specific response codes from this class
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
		return ResponseCode.isSuccess(code.responseCode);
	}

	public static boolean isClientError(final OperationResponseCode code) {
		return ResponseCode.isClientError(code.responseCode);
	}

	public static boolean isServerError(final OperationResponseCode code) {
		return ResponseCode.isServerError(code.responseCode);
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
			else if(operationType == OperationTypes.UPDATE) {
				switch(code) {
					case CHANGED: 		return "\"Update\" operation is completed successfully";
					case BAD_REQUEST: 	return "The mandatory parameter is not specified or unknown parameter is specified";
					default: 			throwError(code, interfaceType, operationType);
				}
			}
			else if(operationType == OperationTypes.DEREGISTER) {
				switch(code) {
					case DELETED: 		return "\"De-register\" operation is completed successfully";
					case NOT_FOUND: 	return "URI of \"De-register\" operation is not found";
					default: 			throwError(code, interfaceType, operationType);
				}
			}

		} else if(interfaceType == InterfaceTypes.REPORTING) {
			if(operationType ==  OperationTypes.OBSERVE) {
				switch(code) {
					case CONTENT: 				return "\"Observe\" operation is completed successfully";
					case BAD_REQUEST: 			return "Target is not allowed for \"Observe\" operation";
					case NOT_FOUND:				return "URI of \"Observe\" operation is not found";
					case METHOD_NOT_ALLOWED:	return "Target is not allowed for \"Observe\" operation";
					default: 					throwError(code, interfaceType, operationType);
				}
			} else if(operationType == OperationTypes.NOTIFY) {
				switch(code) {
					case CHANGED: 		return "\"Notify\" operation is completed successfully";
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