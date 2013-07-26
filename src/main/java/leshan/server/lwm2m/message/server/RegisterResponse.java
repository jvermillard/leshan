package leshan.server.lwm2m.message.server;

import org.apache.commons.lang.Validate;

public class RegisterResponse extends CreatedResponse {

    public RegisterResponse(int id, String... registrationPath) {
        super(id, registrationPath);

        // the registration operation must return a location under /rd
        Validate.isTrue(this.getNewLocation().length >= 2);
        Validate.isTrue("rd".equals(this.getNewLocation()[0]));
    }
}
