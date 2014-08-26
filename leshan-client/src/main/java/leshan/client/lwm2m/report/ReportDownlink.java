package leshan.client.lwm2m.report;

import leshan.client.lwm2m.response.OperationResponse;

public interface ReportDownlink {
	public OperationResponse observe(int objectId, int objectInstanceId, int resourceId);
	public OperationResponse cancelObservation(int objectId, int objectInstanceId, int resourceId);
}
