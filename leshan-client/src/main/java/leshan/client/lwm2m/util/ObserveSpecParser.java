/*
 * Copyright (c) 2013, Sierra Wireless,
 * Copyright (c) 2014, Zebra Technologies,
 * 
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of {{ project }} nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
        case GREATER_THAN:
            return bob.greaterThan(Float.parseFloat(value));
        case LESS_THAN:
            return bob.lessThan(Float.parseFloat(value));
        case STEP:
            return bob.step(Float.parseFloat(value));
        case MIN_PERIOD:
            return bob.minPeriod(Integer.parseInt(value));
        case MAX_PERIOD:
            return bob.maxPeriod(Integer.parseInt(value));
        default:
            throw new IllegalArgumentException();
        }
    }

}
