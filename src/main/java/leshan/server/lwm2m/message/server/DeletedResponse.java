package leshan.server.lwm2m.message.server;

import org.apache.mina.coap.CoapMessage;

public class DeletedResponse implements ServerMessage {

    private final int id;

    public DeletedResponse(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public CoapMessage encode(MessageEncoder visitor) {
        return visitor.encode(this);
    }

}
