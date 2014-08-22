package leshan.client.lwm2m.manage;

import leshan.client.lwm2m.response.OperationResponse;

public interface ManageDownlink {

	OperationResponse read(int objectId, int objectInstaceId, int resourceId);
	OperationResponse read(int objectId, int objectInstaceId);
	OperationResponse read(int objectId);

}
