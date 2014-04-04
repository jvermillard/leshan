package leshan.server.lwm2m.message;

public class RequestTimeoutException extends ResourceAccessException {

    private static final long serialVersionUID = -6372006578730743741L;

    /**
     * Sets all information.
     * 
     * @param uri the resource URI accessed
     * @param timeout the number of milliseconds after which the request has
     *            timed out
     */
    public RequestTimeoutException(String uri, int timeout) {
        super(null, uri, String.format("Request timed out after %d milliseconds", timeout));
    }
}
