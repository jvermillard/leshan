package leshan.client.lwm2m.operation;

import java.util.Arrays;
import java.util.Date;

import leshan.server.lwm2m.observation.ObserveSpec;

public class ObserveNotifyExchange extends ForwardingLwM2mExchange {

	private static final long SECONDS_TO_MILLIS = 1000;

	private ObserveSpec observeSpec;

	private byte[] previousValue;
	private Date previousTime;

	public ObserveNotifyExchange(final LwM2mExchange exchange) {
		super(exchange);
		observeSpec = new ObserveSpec.Builder().build();
	}

	@Override
	public void respond(final LwM2mResponse response) {
		if (previousTime == null) {
			updatePrevious(response);
			exchange.respond(response);
		} else if (shouldNotify(response)) {
			sendNotify(response);
		}
	}

	private void updatePrevious(final LwM2mResponse response) {
		previousValue = response.getResponsePayload();
		previousTime = new Date();
	}

	private void sendNotify(final LwM2mResponse response) {
		updatePrevious(response);
		exchange.respond(ObserveResponse.notifyWithContent(response.getResponsePayload()));
	}

	private boolean shouldNotify(final LwM2mResponse response) {
		final long diff = new Date().getTime() - previousTime.getTime();
		final Integer pmax = observeSpec.getMaxPeriod();
		if (pmax != null && diff > pmax*SECONDS_TO_MILLIS) {
			return true;
		}
		return !Arrays.equals(response.getResponsePayload(), previousValue);
	}

	public void setObserveSpec(final ObserveSpec observeSpec) {
		this.observeSpec = observeSpec;
	}

}
