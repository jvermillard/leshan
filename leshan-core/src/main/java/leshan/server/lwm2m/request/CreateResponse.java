package leshan.server.lwm2m.request;

public class CreateResponse extends ClientResponse {

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
	
}
