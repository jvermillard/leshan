package leshan.server.lwm2m.request;

public abstract class CoapResponseCode {
	
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
	}
	
	public abstract ResponseCode fromCoapCode(int code);

}
