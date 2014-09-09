package leshan.client.lwm2m.resource;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import leshan.client.lwm2m.operation.AggregatedLwM2mExchange;
import leshan.client.lwm2m.operation.LwM2mCreateExchange;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.LwM2mObjectReadResponseAggregator;
import leshan.client.lwm2m.operation.LwM2mResponseAggregator;
import leshan.client.lwm2m.operation.ReadResponse;

public class LwM2mObject {

	private final LwM2mObjectDefinition definition;
	private final AtomicInteger instanceCounter;
	private final Map<Integer, LwM2mObjectInstance> instances;

	public LwM2mObject(final LwM2mObjectDefinition definition) {
		this.definition = definition;
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

	public void handleCreate(final LwM2mCreateExchange exchange) {
		final int newInstanceId = getNewInstanceId(exchange);
		final LwM2mObjectInstance instance = new LwM2mObjectInstance(newInstanceId, definition);
		exchange.setObjectInstance(instance);
		instance.handleCreate(exchange);
	}

	public void onSuccessfulCreate(final LwM2mObjectInstance instance) {
		instances.put(instance.getId(), instance);
	}

	private int getNewInstanceId(final LwM2mExchange exchange) {
		if (exchange.hasObjectInstanceId()) {
			return exchange.getObjectInstanceId();
		} else {
			return instanceCounter.getAndIncrement();
		}
	}

}