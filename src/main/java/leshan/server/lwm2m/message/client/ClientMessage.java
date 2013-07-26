package leshan.server.lwm2m.message.client;

import leshan.server.lwm2m.message.LwM2mMessage;

/**
 * A LW-M2M message from a device.
 */
public interface ClientMessage extends LwM2mMessage {

    /**
     * Processes the message using the visitor pattern.
     * 
     * @param visitor
     * @return the message to send to the client if a response is expected
     */
    LwM2mMessage process(MessageProcessor visitor);

}
