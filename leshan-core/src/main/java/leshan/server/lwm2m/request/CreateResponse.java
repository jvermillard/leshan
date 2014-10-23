package leshan.server.lwm2m.request;

import leshan.server.lwm2m.request.CoapResponseCode.ResponseCode;

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
