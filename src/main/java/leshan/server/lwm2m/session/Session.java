package leshan.server.lwm2m.session;

import org.apache.mina.api.IdleStatus;
import org.apache.mina.api.IoSession;
import org.apache.mina.session.AttributeKey;

/**
 * A session to store some attributes of the connection between a LW-M2M client and the server.
 * <p>
 * This class is a wrapper around the mina {@link IoSession}. It provides some facilities to access the LW-M2M
 * attributes stored in the session.
 * </p>
 */
public class Session {

    private static final AttributeKey<RegistrationState> STATE_KEY = AttributeKey.createKey(RegistrationState.class,
            "registrationState");
    private static final AttributeKey<String> REGISTRATION_ID_KEY = AttributeKey.createKey(String.class,
            "registrationId");

    public enum RegistrationState {
        UNREGISTERED, REGISTERED
    }

    private final IoSession ioSession;

    public Session(IoSession ioSession) {
        this.ioSession = ioSession;
    }

    public RegistrationState getState() {
        return ioSession.getAttribute(STATE_KEY);
    }

    public void setState(RegistrationState state) {
        ioSession.setAttribute(STATE_KEY, state);
    }

    public String getRegistrationId() {
        return ioSession.getAttribute(REGISTRATION_ID_KEY);
    }

    public void setRegistrationId(String registrationId) {
        ioSession.setAttribute(REGISTRATION_ID_KEY, registrationId);
    }

    /**
     * @param lifeTime the new lifetime value in second
     */
    public void updateLifeTime(long lifeTime) {
        ioSession.getConfig().setIdleTimeInMillis(IdleStatus.READ_IDLE, lifeTime * 1000);
    }

    /**
     * Close the session after the server response
     */
    public void close() {
        ioSession.close(false);
    }

}
