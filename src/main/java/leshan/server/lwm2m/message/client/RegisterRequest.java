package leshan.server.lwm2m.message.client;

import java.util.Arrays;

import leshan.server.lwm2m.message.LwM2mMessage;
import leshan.server.lwm2m.session.BindingMode;
import leshan.server.lwm2m.session.LwSession;

import org.apache.commons.lang.Validate;

/**
 * The message sent by the the client to the server to perform a <b>Register</b> operation.
 */
public class RegisterRequest implements ClientRequest {

    private final int id;

    /** the LWM2M Client identifier on one LWM2M Server */
    private final String endpoint;

    /**
     * the registration is removed by the server if a new registration or update is not received within this lifetime
     * (in second)
     */
    private final long lifetime;

    /** the LW-M2M version supported by the client */
    private final String lwM2mVersion;

    /** the current transport binding and queue mode */
    private final BindingMode bindingMode;

    /** MSISDN where the client can be reached for use with the SMS binding */
    private final String smsNumber;

    /** the list of Objects supported and Object Instances available on the client */
    private final String[] objects;

    /**
     * Create a LW-M2M Register message
     * 
     * @param endpoint
     * @param lifetime Optional with default value <i>86400</i>.
     * @param lwM2mVersion Optional with default value <i>1.0</i>.
     * @param bindingMode Optional with default value {@link BindingMode#U}.
     * @param smsNumber Optional.
     * @param objects
     */
    public RegisterRequest(int id, String endpoint, Long lifetime, String lwM2mVersion, BindingMode bindingMode,
            String smsNumber, String[] objects) {

        Validate.notEmpty(endpoint);
        Validate.notEmpty(objects);

        this.id = id;
        this.endpoint = endpoint;
        this.lifetime = lifetime == null ? 86400 : lifetime;
        this.lwM2mVersion = lwM2mVersion == null ? "1.0" : lwM2mVersion;
        this.bindingMode = bindingMode == null ? BindingMode.U : bindingMode;
        this.smsNumber = smsNumber;
        this.objects = objects;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LwM2mMessage process(RequestProcessor visitor, LwSession session) {
        return visitor.process(this, session);
    }

    public int getId() {
        return id;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public long getLifetime() {
        return lifetime;
    }

    public String getLwM2mVersion() {
        return lwM2mVersion;
    }

    public BindingMode getBindingMode() {
        return bindingMode;
    }

    public String getSmsNumber() {
        return smsNumber;
    }

    public String[] getObjects() {
        return objects;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RegisterMessage [id=").append(id).append(", endpoint=").append(endpoint).append(", lifetime=")
                .append(lifetime).append(", lwM2mVersion=").append(lwM2mVersion).append(", bindingMode=")
                .append(bindingMode).append(", smsNumber=").append(smsNumber).append(", objects=")
                .append(Arrays.toString(objects)).append("]");
        return builder.toString();
    }

}
