/*
 * Copyright (c) 2013, Sierra Wireless
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
package leshan;

import java.util.LinkedList;
import java.util.List;

/**
 * A container for attributes describing the intended behavior of a LWM2M Client regarding sending notifications for an
 * observed resource.
 * 
 * The Lightweight M2M spec defines the following attributes:
 * <ul>
 * <li>minimum period</li>
 * <li>maximum period</li>
 * <li>greater than</li>
 * <li>less than</li>
 * <li>step</li>
 * <li>cancel</li>
 * </ul>
 */
public final class ObserveSpec {

    private static final String PARAM_MIN_PERIOD = "pmin=%s";
    private static final String PARAM_MAX_PERIOD = "pmax=%s";
    private static final String PARAM_GREATER_THAN = "gt=%s";
    private static final String PARAM_LESS_THAN = "lt=%s";
    private static final String PARAM_STEP = "st=%s";
    private static final String PARAM_CANCEL = "cancel";
    private Integer minPeriod;
    private Integer maxPeriod;
    private Float greaterThan;
    private Float lessThan;
    private Float step;
    private boolean cancel;

    private ObserveSpec() {
    }

    public Integer getMinPeriod() {
        return this.minPeriod;
    }

    public Integer getMaxPeriod() {
        return this.maxPeriod;
    }

    public Float getGreaterThan() {
        return this.greaterThan;
    }

    public Float getLessThan() {
        return this.lessThan;
    }

    public Float getStep() {
        return this.step;
    }

    public Boolean getCancel() {
        return this.cancel;
    }

    public String[] toQueryParams() {
        List<String> queries = new LinkedList<>();
        if (this.cancel) {
            queries.add(PARAM_CANCEL);
        } else {
            if (this.minPeriod != null) {
                queries.add(String.format(PARAM_MIN_PERIOD, this.minPeriod));
            }
            if (this.maxPeriod != null) {
                queries.add(String.format(PARAM_MAX_PERIOD, this.maxPeriod));
            }
            if (this.lessThan != null) {
                queries.add(String.format(PARAM_LESS_THAN, this.lessThan));
            }
            if (this.greaterThan != null) {
                queries.add(String.format(PARAM_GREATER_THAN, this.greaterThan));
            }
            if (this.step != null) {
                queries.add(String.format(PARAM_STEP, this.step));
            }
        }
        return queries.toArray(new String[queries.size()]);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (String query : toQueryParams()) {
            b.append(query).append("&");
        }
        return b.toString();
    }

    /**
     * A builder for ObserveSpec instances.
     * 
     * Provides a <em>fluid API</em> for setting attributes. Creating an ObserveSpec instance works like this:
     * 
     * <pre>
     * ObserveSpec spec = new ObserveSpec.Builder().minPeriod(10).greaterThan(34.12).build();
     * </pre>
     */
    public static class Builder {

        private boolean cancel;
        private Float step;
        private Float greaterThan;
        private Float lessThan;
        private Integer minPeriod;
        private Integer maxPeriod;

        public Builder() {
            super();
        }

        public ObserveSpec build() {
            ObserveSpec spec = new ObserveSpec();
            if (this.cancel) {
                spec.cancel = Boolean.TRUE;
                return spec;
            }

            if (this.maxPeriod != null && this.minPeriod != null && this.maxPeriod < this.minPeriod) {
                throw new IllegalStateException("minPeriod must be smaller than maxPeriod");
            } else {
                spec.greaterThan = this.greaterThan;
                spec.lessThan = this.lessThan;
                spec.minPeriod = this.minPeriod;
                spec.maxPeriod = this.maxPeriod;
                spec.step = this.step;
                return spec;
            }
        }

        public Builder cancel() {
            this.cancel = true;
            return this;
        }

        public Builder step(float step) {
            this.step = step;
            return this;
        }

        public Builder greaterThan(float threshold) {
            this.greaterThan = threshold;
            return this;
        }

        public Builder lessThan(float threshold) {
            this.lessThan = threshold;
            return this;
        }

        public Builder minPeriod(int seconds) {
            this.minPeriod = seconds;
            return this;
        }

        public Builder maxPeriod(int seconds) {
            this.maxPeriod = seconds;
            return this;
        }

    }
}
