package leshan.client.lwm2m.resource;

import leshan.client.lwm2m.operation.ReadResponse;

public interface Notifier {

	void notify(ReadResponse notification);

}
