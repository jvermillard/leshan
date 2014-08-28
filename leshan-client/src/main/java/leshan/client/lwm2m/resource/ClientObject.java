package leshan.client.lwm2m.resource;

import java.util.Arrays;
import java.util.List;

public class ClientObject {

	private int objectId = 1;
	
	private ClientObject(){
		
	}
	
	public ClientObject(final int objectId){
		this.objectId = objectId;
	}

	public int getObjectId() {
		return objectId;
	}

	public List<Integer> getResourceIds() {
		return Arrays.asList(0, 1);
	}

}
