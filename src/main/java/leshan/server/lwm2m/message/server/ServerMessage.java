package leshan.server.lwm2m.message.server;

import leshan.server.lwm2m.message.LwM2mMessage;

import org.apache.mina.coap.CoapMessage;

public interface ServerMessage extends LwM2mMessage {

    /**
     * Encodes the message using the visitor pattern.
     * 
     * @param visitor
     * @return the CoAP message to be returned to the client
     */
    CoapMessage encode(MessageEncoder visitor);

}
