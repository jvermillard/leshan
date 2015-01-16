package leshan.core.response;

import leshan.ResponseCode;

public class CreateResponse extends LwM2mResponse {

    private String location;

    public CreateResponse(ResponseCode code) {
        super(code);
    }

    public CreateResponse(ResponseCode code, String location) {
        super(code);
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return String.format("CreateResponse [location=%s, code=%s]", location, code);
    }

}
