package leshan.client.lwm2m.manage;

import java.util.List;

import leshan.client.lwm2m.response.OperationResponse;

public interface ManageDownlink {

	OperationResponse read(int objectId, int objectInstanceId, int resourceId);
	OperationResponse read(int objectId, int objectInstanceId);
	OperationResponse read(int objectId);

	OperationResponse discover(int objectId, int objectInstanceId, int resourceId);
	OperationResponse discover(int objectId, int objectInstanceId);
	OperationResponse discover(int objectId);

	OperationResponse replace(int objectId, int objectInstanceId, int resourceId, String newValue);
	OperationResponse replace(int objectId, int objectInstanceId, String newValue);

	OperationResponse writeAttributes(int objectId, int objectInstanceId, int resourceId, List<String> queries);
	OperationResponse writeAttributes(int objectId, int objectInstanceId, List<String> queries);
	OperationResponse writeAttributes(int objectId, List<String> queries);

	OperationResponse create(int objectId, String payload);

	OperationResponse delete(int objectId, int objectInstanceId);

	// TODO: The existence of these two methods is reflective of ambiguities in the specification.
	// It would be ideal to find a way of clearing up this ambiguity, most likely by exposing some
	// sort of Resource model to the ManageMessageDeliverer.
	OperationResponse partialUpdateOrExecute(int objectId, int objectInstanceId, int resourceId, String payload);
	OperationResponse partialUpdateOrCreate(int objectId, int objectInstanceId, String newValue);
}
