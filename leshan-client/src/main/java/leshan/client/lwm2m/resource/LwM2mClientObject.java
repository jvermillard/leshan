package leshan.client.lwm2m.resource;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import leshan.client.lwm2m.exchange.LwM2mCreateExchange;
import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.exchange.ObserveNotifyExchange;
import leshan.client.lwm2m.exchange.aggregate.AggregatedLwM2mExchange;
import leshan.client.lwm2m.exchange.aggregate.LwM2mObjectReadResponseAggregator;
import leshan.client.lwm2m.exchange.aggregate.LwM2mResponseAggregator;
import leshan.client.lwm2m.response.CreateResponse;
import leshan.client.lwm2m.response.ReadResponse;
import leshan.client.lwm2m.response.WriteResponse;
import leshan.server.lwm2m.observation.ObserveSpec;

public class LwM2mClientObject implements LwM2mClientNode {

	private final LwM2mClientObjectDefinition definition;
	private final AtomicInteger instanceCounter;
	private final Map<Integer, LwM2mClientObjectInstance> instances;
	private ObserveSpec observeSpec;

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

	public void handleCreate(final LwM2mCreateExchange exchange) {
		if(instanceCounter.get() >= 1 && definition.isSingle()) {
			exchange.respond(CreateResponse.invalidResource());
		}

		final LwM2mClientObjectInstance instance = createNewInstance(exchange.hasObjectInstanceId(), exchange.getObjectInstanceId());
		exchange.setObjectInstance(instance);
		instance.handleCreate(exchange);
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
	public void observe(LwM2mExchange exchange, ScheduledExecutorService service) {
		new ObserveNotifyExchange(exchange, this, observeSpec, service);
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

	@Override
	public void writeAttributes(LwM2mExchange exchange, ObserveSpec spec) {
		this.observeSpec = spec;
		exchange.respond(WriteResponse.success());
	}

}