package leshan.server.lwm2m;

import leshan.server.lwm2m.message.LwM2mMessage;
import leshan.server.lwm2m.message.client.MessageProcessor;
import leshan.server.lwm2m.message.client.RegisterMessage;
import leshan.server.lwm2m.message.server.RegisterResponse;

public class LwM2mProcessor implements MessageProcessor {

    @Override
    public LwM2mMessage process(RegisterMessage message) {
        // TODO
        return new RegisterResponse(message.getId(), "rd", "12345678");
    }

}
