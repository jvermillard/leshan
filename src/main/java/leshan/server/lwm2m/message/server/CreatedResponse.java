package leshan.server.lwm2m.message.server;

import java.util.Arrays;

import org.apache.commons.lang.Validate;
import org.apache.mina.coap.CoapMessage;

public class CreatedResponse implements ServerMessage {

    private final int id;
    private final String[] newLocation;

    public CreatedResponse(int id, String... newLocation) {
        Validate.notEmpty(newLocation);

        this.id = id;
        this.newLocation = newLocation;
    }

    public int getId() {
        return id;
    }

    public String[] getNewLocation() {
        return newLocation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CoapMessage encode(MessageEncoder visitor) {
        return visitor.encode(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CreatedResponse [id=").append(id).append(", newLocation=").append(Arrays.toString(newLocation))
                .append("]");
        return builder.toString();
    }

}
