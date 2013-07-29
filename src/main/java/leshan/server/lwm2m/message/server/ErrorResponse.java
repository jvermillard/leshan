package leshan.server.lwm2m.message.server;

import leshan.server.lwm2m.message.ResponseCode;

import org.apache.mina.coap.CoapMessage;

public class ErrorResponse implements ServerMessage {

    private final int id;
    private final ResponseCode errorCode;

    public ErrorResponse(int id, ResponseCode errorCode) {
        this.id = id;
        this.errorCode = errorCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getId() {
        return id;
    }

    public ResponseCode getErrorCode() {
        return errorCode;
    }

    @Override
    public CoapMessage encode(MessageEncoder visitor) {
        return visitor.encode(this);
    }

}
