package leshan.server.lwm2m.security;

public class NonUniqueSecurityInfoException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a {@code NonUniqueSecurityInfoException} with the specified message and root cause.
     *
     * @param msg the detail message
     * @param t the root cause
     */
    public NonUniqueSecurityInfoException(String msg, Throwable t) {
        super(msg, t);
    }

    /**
     * Constructs a {@code NonUniqueSecurityInfoException} with the specified message and no root cause.
     *
     * @param msg the detail message
     */
    public NonUniqueSecurityInfoException(String msg) {
        super(msg);
    }

}
