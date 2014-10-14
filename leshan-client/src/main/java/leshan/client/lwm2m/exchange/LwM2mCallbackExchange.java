package leshan.client.lwm2m.exchange;

import leshan.client.lwm2m.resource.LwM2mClientNode;

public interface LwM2mCallbackExchange<T extends LwM2mClientNode> extends LwM2mExchange {

	void setNode(T node);

}
