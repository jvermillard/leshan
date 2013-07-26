package leshan.server.lwm2m.message;

/**
 * Common interface for all LW-M2M messages
 */
public interface LwM2mMessage {

    /**
     * @return the message identifier used as ticketId in the request/response sequence
     */
    int getId();

}
