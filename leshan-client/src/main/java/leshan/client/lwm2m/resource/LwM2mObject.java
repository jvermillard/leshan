package leshan.client.lwm2m.resource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import leshan.client.lwm2m.operation.AggregatedLwM2mExchange;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.LwM2mObjectReadResponseAggregator;
import leshan.client.lwm2m.operation.LwM2mResponseAggregator;
import leshan.client.lwm2m.operation.ReadResponse;

public class LwM2mObject {

	private final Map<Integer, LwM2mResourceDefinition> definitionMap;
		private final AtomicInteger instanceCounter;
	private final Map<Integer, LwM2mObjectInstance> instances;

	public LwM2mObject(final LwM2mResourceDefinition... definitions) {
		definitionMap = new HashMap<>();
		for (final LwM2mResourceDefinition def : definitions) {
			definitionMap.put(def.getId(), def);
		}
				this.instanceCounter = new AtomicInteger(0);
		this.instances = new ConcurrentHashMap<>();
	}

	public void handleRead(final LwM2mExchange exchange) {
		final Collection<LwM2mObjectInstance> instances = this.instances.values();

		if (instances.isEmpty()) {
			exchange.respond(ReadResponse.success(new byte[0]));
			return;
		}

		final LwM2mResponseAggregator aggr = new LwM2mObjectReadResponseAggregator(
				exchange,
				instances.size());
		for (final LwM2mObjectInstance inst : instances) {
			inst.handleNormalRead(new AggregatedLwM2mExchange(aggr, inst.getId()));
		}
	}

	public LwM2mObjectInstance createInstance(final LwM2mExchange exchange) {
		final int newInstanceId = getNewInstanceId(exchange);
		final LwM2mObjectInstance instance = LwM2mObjectInstance.instantiate(newInstanceId, exchange, definitionMap);
		if (instance != null) {
			instances.put(newInstanceId, instance);
		}
		return instance;
	}

	private int getNewInstanceId(final LwM2mExchange exchange) {
		if (exchange.hasObjectInstanceId()) {
			return exchange.getObjectInstanceId();
		} else {
			return instanceCounter.getAndIncrement();
		}
	}

}