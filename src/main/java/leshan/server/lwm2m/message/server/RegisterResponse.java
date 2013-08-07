package leshan.server.lwm2m.message.server;

public class RegisterResponse extends CreatedResponse {

    public RegisterResponse(int id, String registrationId) {
        super(id, "rd", registrationId);
    }
}
