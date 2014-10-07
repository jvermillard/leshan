package leshan.client.lwm2m.operation;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import leshan.client.lwm2m.exchange.ForwardingLwM2mExchange;
import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.LwM2mClientResource;
import leshan.client.lwm2m.response.LwM2mResponse;
import leshan.client.lwm2m.response.ObserveResponse;
import leshan.server.lwm2m.observation.ObserveSpec;

public class ObserveNotifyExchange extends ForwardingLwM2mExchange implements Runnable {

	private static final long SECONDS_TO_MILLIS = 1000;

	private ObserveSpec observeSpec;

	private ScheduledExecutorService service;
	private LwM2mClientResource resource;
	private byte[] previousValue;
	private Date previousTime;

	public ObserveNotifyExchange(final LwM2mExchange exchange,
			LwM2mClientResource resource,
			ObserveSpec observeSpec,
			ScheduledExecutorService service) {
		super(exchange);
		this.resource = resource;
		this.observeSpec = observeSpec;
		this.service = service;
		updatePrevious(null);
		scheduleNext();
	}

	@Override
	public void respond(final LwM2mResponse response) {
		if (shouldNotify(response)) {
			sendNotify(response);
		}
		scheduleNext();
	}

	private void updatePrevious(byte[] responsePayload) {
		previousValue = responsePayload;
		previousTime = new Date();
	}

	private boolean shouldNotify(final LwM2mResponse response) {
		final long diff = getTimeDiff();
		final Integer pmax = observeSpec.getMaxPeriod();
		if (pmax != null && diff > pmax*SECONDS_TO_MILLIS) {
			return true;
		}
		return !Arrays.equals(response.getResponsePayload(), previousValue);
	}

	private void sendNotify(final LwM2mResponse response) {
		updatePrevious(response.getResponsePayload());
		exchange.respond(ObserveResponse.notifyWithContent(response.getResponsePayload()));
	}

	public void setObserveSpec(final ObserveSpec observeSpec) {
		this.observeSpec = observeSpec;
	}

	private void scheduleNext() {
		if (observeSpec.getMaxPeriod() != null) {
			long diff = getTimeDiff();
			service.schedule(this,
					observeSpec.getMaxPeriod()*SECONDS_TO_MILLIS - diff,
					TimeUnit.MILLISECONDS);
		}
	}

	private long getTimeDiff() {
		return new Date().getTime() - previousTime.getTime();
	}

	@Override
	public void run() {
		resource.read(this);
	}
	
}
