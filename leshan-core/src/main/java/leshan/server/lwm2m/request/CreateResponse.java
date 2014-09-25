package leshan.server.lwm2m.request;

public class CreateResponse extends ClientResponse {

	public CreateResponse(ResponseCode code) {
		super(code);
	}
	
	public CreateResponse(ResponseCode code, String location) {
		super(code);
	}
	
	public String getLocation() {
		return null;
	}
	
}
