package leshan.client.lwm2m.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ch.ethz.inf.vs.californium.network.Exchange;
import ch.ethz.inf.vs.californium.server.resources.Resource; // TODO: make our resource

public enum Resources {
	INSTANCE;

	private final Map<Resource, List<Exchange>> resourcesMap = new ConcurrentHashMap<>();

	public void addResource(final Resource resource) {
		if(!resourcesMap.containsKey(resource)) {
			resourcesMap.put(resource, new ArrayList<Exchange>());
		}
	}

	List<Exchange> getExchangesForResource(final Resource resource) {
		return resourcesMap.get(resource);
	}
}
