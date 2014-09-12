package leshan.client.lwm2m.util;

import java.util.Arrays;
import java.util.List;

import leshan.server.lwm2m.observation.ObserveSpec;
import leshan.server.lwm2m.observation.ObserveSpec.Builder;

public class ObserveSpecParser {

	private static final String CANCEL = "cancel";

	private static final String GREATER_THAN = "gt";
	private static final String LESS_THAN = "lt";
	private static final String MAX_PERIOD = "pmax";
	private static final String MIN_PERIOD = "pmin";
	private static final String STEP = "st";

	public static ObserveSpec parse(final List<String> uriQueries) {
		ObserveSpec.Builder builder = new ObserveSpec.Builder();
		if (uriQueries.equals(Arrays.asList(CANCEL))) {
			return builder.cancel().build();
		}
		for (final String query : uriQueries) {
			builder = process(builder, query);
		}
		return builder.build();
	}

	private static Builder process(final ObserveSpec.Builder bob, final String query) {
		final String[] split = query.split("=");
		if (split.length != 2) {
			throw new IllegalArgumentException();
		}

		final String key = split[0];
		final String value = split[1];

		switch (key) {
		case GREATER_THAN: return bob.greaterThan(Float.parseFloat(value));
		case LESS_THAN: return bob.lessThan(Float.parseFloat(value));
		case STEP: return bob.step(Float.parseFloat(value));
		case MIN_PERIOD: return bob.minPeriod(Integer.parseInt(value));
		case MAX_PERIOD: return bob.maxPeriod(Integer.parseInt(value));
		default: throw new IllegalArgumentException();
		}
	}

}
