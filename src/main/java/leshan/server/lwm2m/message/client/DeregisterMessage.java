package leshan.server.lwm2m.message.client;

import leshan.server.lwm2m.message.LwM2mMessage;
import leshan.server.lwm2m.session.LwSession;

import org.apache.commons.lang.Validate;

/**
 * The message sent by the the client to the server to perform a <b>De-registration</b> operation.
 */
public class DeregisterMessage implements ClientMessage {

    private final int id;

    /** the identifier used to unregister the client */
    private final String registrationId;

    public DeregisterMessage(int id, String registrationId) {
        Validate.notEmpty(registrationId);

        this.id = id;
        this.registrationId = registrationId;
    }

    @Override
    public LwM2mMessage process(MessageProcessor visitor, LwSession session) {
        return visitor.process(this, session);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getId() {
        return id;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DeregisterMessage [id=").append(id).append(", registrationId=").append(registrationId)
                .append("]");
        return builder.toString();
    }

}
