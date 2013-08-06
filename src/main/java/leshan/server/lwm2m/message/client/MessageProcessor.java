package leshan.server.lwm2m.message.client;

import leshan.server.lwm2m.message.LwM2mMessage;
import leshan.server.lwm2m.session.Session;

/**
 * A visitor to process the incoming LW-M2M messages
 */
public interface MessageProcessor {

    LwM2mMessage process(RegisterMessage message, Session session);

    LwM2mMessage process(DeregisterMessage message, Session session);

    void sessionClosed(Session session);
}
