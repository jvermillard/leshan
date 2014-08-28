package leshan.client.lwm2m.resource;

import java.util.Arrays;
import java.util.List;

public class ClientObject {

	public int getObjectId() {
		return 1;
	}

	public List<Integer> getResourceIds() {
		return Arrays.asList(0, 1);
	}

}
