package leshan.server.lwm2m.message.client;

import leshan.server.lwm2m.message.LwM2mMessage;
import leshan.server.lwm2m.session.LwSession;

/**
 * A request from a LW-M2M device.
 */
public interface ClientRequest extends LwM2mMessage {

    /**
     * Processes the message using the visitor pattern.
     * 
     * @param visitor the message processor
     * @param session the current session
     * @return the message to send to the client if a response is expected
     */
    LwM2mMessage process(RequestProcessor visitor, LwSession session);

}
