package leshan.server.lwm2m.message.client;

import leshan.server.lwm2m.message.LwM2mMessage;
import leshan.server.lwm2m.session.LwSession;

/**
 * A visitor to process the incoming LW-M2M messages
 */
public interface RequestProcessor {

    LwM2mMessage process(RegisterRequest message, LwSession session);

    LwM2mMessage process(DeregisterRequest message, LwSession session);

    void sessionClosed(LwSession session);
}
