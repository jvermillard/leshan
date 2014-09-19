package leshan.client.lwm2m.resource;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import leshan.client.lwm2m.operation.ExecuteResponse;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.ObserveNotifyExchange;
import leshan.client.lwm2m.operation.ReadResponse;
import leshan.client.lwm2m.operation.WriteResponse;
import leshan.server.lwm2m.observation.ObserveSpec;

public abstract class BaseTypedLwM2mResource<E extends TypedLwM2mExchange<?>> implements LwM2mResource {

	protected abstract E createSpecificExchange(final LwM2mExchange exchange);

	private final Set<ObserveNotifyExchange> observers = new HashSet<>();
	private ObserveSpec observeSpec;

	public BaseTypedLwM2mResource() {
		final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				notifyResourceUpdated();
			}

		}, 0, 1, TimeUnit.SECONDS);

		this.observeSpec = new ObserveSpec.Builder().build();
	}

	@Override
	public final void read(final LwM2mExchange exchange) {
		if(exchange.isObserve() && !observers.contains(exchange)) {
			final ObserveNotifyExchange observeNotifyExchange = new ObserveNotifyExchange(exchange);
			observers.add(observeNotifyExchange);
			handleRead(createSpecificExchange(observeNotifyExchange));
		} else {
			handleRead(createSpecificExchange(exchange));
		}
	}

	protected void handleRead(final E exchange) {
		exchange.advanced().respond(ReadResponse.notAllowed());
	}

	@Override
	public final void write(final LwM2mExchange exchange) {
		try {
			final ObserveSpec spec = exchange.getObserveSpec();
			if (spec != null) {
				handleWriteAttributes(exchange, spec);
			} else {
				handleWrite(createSpecificExchange(exchange));
			}
		} catch(final Exception e) {
			exchange.respond(WriteResponse.badRequest());
		}
	}

	private void handleWriteAttributes(final LwM2mExchange exchange, final ObserveSpec spec) {
		observeSpec = spec;
		exchange.respond(WriteResponse.success());
	}

	protected void handleWrite(final E exchange) {
		exchange.advanced().respond(WriteResponse.notAllowed());
	}

	@Override
	public void execute(final LwM2mExchange exchange) {
		handleExecute(exchange);
	}

	public void handleExecute(final LwM2mExchange exchange) {
		exchange.respond(ExecuteResponse.notAllowed());
	}

	@Override
	public void observe(final Notifier notifier) {

	}

	@Override
	public boolean isReadable() {
		return false;
	}

	@Override
	public final void notifyResourceUpdated() {
		for(final ObserveNotifyExchange exchange : observers) {
			exchange.setObserveSpec(observeSpec);
			read(exchange);
		}
	}

}
