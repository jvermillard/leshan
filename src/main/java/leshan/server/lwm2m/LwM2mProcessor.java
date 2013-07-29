package leshan.server.lwm2m;

import leshan.server.lwm2m.message.LwM2mMessage;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.message.client.DeregisterMessage;
import leshan.server.lwm2m.message.client.MessageProcessor;
import leshan.server.lwm2m.message.client.RegisterMessage;
import leshan.server.lwm2m.message.server.DeletedResponse;
import leshan.server.lwm2m.message.server.ErrorResponse;
import leshan.server.lwm2m.message.server.RegisterResponse;
import leshan.server.lwm2m.session.Session;
import leshan.server.lwm2m.session.Session.RegistrationState;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LwM2mProcessor implements MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(LwM2mProcessor.class);

    @Override
    public LwM2mMessage process(RegisterMessage message, Session session) {
        LOG.debug("processing a register message : " + message);

        if (session.getState() != null) {
            // the client should not be already registered
            return new ErrorResponse(message.getId(), ResponseCode.CONFLICT);
        }

        session.setState(RegistrationState.REGISTERED);

        String registrationId = this.createRegistrationId();
        session.setRegistrationId(registrationId);

        // TODO store registration parameters in the session

        session.updateLifeTime(message.getLifetime());

        return new RegisterResponse(message.getId(), registrationId);
    }

    private String createRegistrationId() {
        return RandomStringUtils.random(10, true, true);
    }

    @Override
    public LwM2mMessage process(DeregisterMessage message, Session session) {
        LOG.debug("processing a deregister message : " + message);

        // check registration location
        if (!message.getRegistrationId().equals(session.getRegistrationId())) {
            LOG.error("invalid registration id, expected '{}', was '{}'", session.getRegistrationId(),
                    message.getRegistrationId());
            return new ErrorResponse(message.getId(), ResponseCode.BAD_REQUEST); // location not found
        }

        // check state
        if (!RegistrationState.REGISTERED.equals(session.getState())) {
            LOG.error("invalid session state, expected 'REGISTERED', was '{}'", session.getState());
            return new ErrorResponse(message.getId(), ResponseCode.BAD_REQUEST);
        }

        session.setState(RegistrationState.UNREGISTERED);
        session.close();

        return new DeletedResponse(message.getId());
    }
}
