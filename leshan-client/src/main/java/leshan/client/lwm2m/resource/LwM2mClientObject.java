package leshan.client.lwm2m.resource;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import leshan.client.lwm2m.exchange.LwM2mCreateExchange;
import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.exchange.aggregate.AggregatedLwM2mExchange;
import leshan.client.lwm2m.exchange.aggregate.LwM2mObjectReadResponseAggregator;
import leshan.client.lwm2m.exchange.aggregate.LwM2mResponseAggregator;
import leshan.client.lwm2m.response.CreateResponse;
import leshan.client.lwm2m.response.ReadResponse;
import leshan.client.lwm2m.response.WriteResponse;

public class LwM2mClientObject extends LwM2mClientNode {

	private final LwM2mClientObjectDefinition definition;
	private final AtomicInteger instanceCounter;
	private final Map<Integer, LwM2mClientObjectInstance> instances;

	public LwM2mClientObject(final LwM2mClientObjectDefinition definition) {
		this.definition = definition;
		this.instanceCounter = new AtomicInteger(0);
		this.instances = new ConcurrentHashMap<>();
	}

	public LwM2mClientObjectInstance createMandatoryInstance() {
		LwM2mClientObjectInstance instance = createNewInstance(false, 0);
		instance.createMandatory();
		return instance;
	}

	public void createInstance(final LwM2mCreateExchange exchange) {
		if(instanceCounter.get() >= 1 && definition.isSingle()) {
			exchange.respond(CreateResponse.invalidResource());
		}

		final LwM2mClientObjectInstance instance = createNewInstance(exchange.hasObjectInstanceId(), exchange.getObjectInstanceId());
		exchange.setObjectInstance(instance);
		instance.createInstance(exchange);
	}

	@Override
	public void read(LwM2mExchange exchange) {
		final Collection<LwM2mClientObjectInstance> instances = this.instances.values();

		if (instances.isEmpty()) {
			exchange.respond(ReadResponse.success(new byte[0]));
			return;
		}

		final LwM2mResponseAggregator aggr = new LwM2mObjectReadResponseAggregator(
				exchange,
				instances.size());
		for (final LwM2mClientObjectInstance inst : instances) {
			inst.read(new AggregatedLwM2mExchange(aggr, inst.getId()));
		}
	}

	@Override
	public void write(LwM2mExchange exchange) {
		exchange.respond(WriteResponse.notAllowed());
	}

	private LwM2mClientObjectInstance createNewInstance(boolean hasObjectInstanceId, int objectInstanceId) {
		final int newInstanceId = getNewInstanceId(hasObjectInstanceId, objectInstanceId);
		final LwM2mClientObjectInstance instance = new LwM2mClientObjectInstance(newInstanceId, definition);
		return instance;
	}

	public void onSuccessfulCreate(final LwM2mClientObjectInstance instance) {
		instances.put(instance.getId(), instance);
	}

	private int getNewInstanceId(boolean hasObjectInstanceId, int objectInstanceId) {
		if (hasObjectInstanceId) {
			return objectInstanceId;
		} else {
			return instanceCounter.getAndIncrement();
		}
	}

}