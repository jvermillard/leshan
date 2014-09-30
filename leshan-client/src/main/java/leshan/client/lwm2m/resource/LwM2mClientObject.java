package leshan.client.lwm2m.resource;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import leshan.client.lwm2m.operation.AggregatedLwM2mExchange;
import leshan.client.lwm2m.operation.CreateResponse;
import leshan.client.lwm2m.operation.LwM2mCreateExchange;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.LwM2mObjectReadResponseAggregator;
import leshan.client.lwm2m.operation.LwM2mResponseAggregator;
import leshan.client.lwm2m.operation.ReadResponse;

public class LwM2mClientObject {

	private final LwM2mClientObjectDefinition definition;
	private final AtomicInteger instanceCounter;
	private final Map<Integer, LwM2mClientObjectInstance> instances;

	public LwM2mClientObject(final LwM2mClientObjectDefinition definition) {
		this.definition = definition;
		this.instanceCounter = new AtomicInteger(0);
		this.instances = new ConcurrentHashMap<>();

//		if(definition.isMandatory()) {
//			handleCreate(new MandatoryObjectExchange());
//		}
	}

	public void handleRead(final LwM2mExchange exchange) {
		final Collection<LwM2mClientObjectInstance> instances = this.instances.values();

		if (instances.isEmpty()) {
			exchange.respond(ReadResponse.success(new byte[0]));
			return;
		}

		final LwM2mResponseAggregator aggr = new LwM2mObjectReadResponseAggregator(
				exchange,
				instances.size());
		for (final LwM2mClientObjectInstance inst : instances) {
			inst.handleRead(new AggregatedLwM2mExchange(aggr, inst.getId()));
		}
	}

	public void handleCreate(final LwM2mCreateExchange exchange) {
		if(instanceCounter.get() >= 1 && definition.isSingle()) {
			exchange.respond(CreateResponse.invalidResource());
		}

		final int newInstanceId = getNewInstanceId(exchange);
		final LwM2mClientObjectInstance instance = new LwM2mClientObjectInstance(newInstanceId, definition);
		exchange.setObjectInstance(instance);
		instance.handleCreate(exchange);
	}

	public void onSuccessfulCreate(final LwM2mClientObjectInstance instance) {
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