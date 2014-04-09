package leshan.server.lwm2m.client;

/**
 * An exception raised when a client registry operation has failed.
 */
public class ClientRegistrationException extends Exception {

    private static final long serialVersionUID = 1L;

    public ClientRegistrationException(String message) {
        super(message);
    }

    public ClientRegistrationException(Throwable t) {
        super(t);
    }

    public ClientRegistrationException(String message, Throwable t) {
        super(message, t);
    }

}
