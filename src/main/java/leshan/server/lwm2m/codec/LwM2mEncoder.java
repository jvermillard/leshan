package leshan.server.lwm2m.codec;

import leshan.server.lwm2m.message.LwM2mMessage;

import org.apache.mina.coap.CoapMessage;
import org.apache.mina.codec.StatelessProtocolEncoder;

public class LwM2mEncoder implements StatelessProtocolEncoder<LwM2mMessage, CoapMessage> {

    @Override
    public Void createEncoderState() {
        return null;
    }

    @Override
    public CoapMessage encode(LwM2mMessage arg0, Void arg1) {
        // TODO Auto-generated method stub
        return null;
    }

}
