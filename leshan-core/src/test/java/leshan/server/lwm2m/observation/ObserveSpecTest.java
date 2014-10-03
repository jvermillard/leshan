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
package leshan.server.lwm2m.observation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ObserveSpecTest {

    ObserveSpec spec;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testToQueryParamsContainsGreaterThan() {
        Float threshold = 12.1f;
        this.spec = new ObserveSpec.Builder().greaterThan(threshold).build();
        Assert.assertEquals(String.format("gt=%s", threshold), this.spec.toQueryParams()[0]);
    }

    @Test
    public void testToQueryParamsContainsMaxPeriod() {
        int seconds = 60;
        this.spec = new ObserveSpec.Builder().maxPeriod(seconds).build();
        Assert.assertEquals(String.format("pmax=%s", seconds), this.spec.toQueryParams()[0]);
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderDetectsInconsistentParams() {
        new ObserveSpec.Builder().minPeriod(5).maxPeriod(2).build();
    }

    @Test
    public void testToQueryParamsContainsStep() {
        Float step = 12.1f;
        this.spec = new ObserveSpec.Builder().step(step).build();
        Assert.assertEquals(String.format("st=%s", step), this.spec.toQueryParams()[0]);
    }

    @Test
    public void testToQueryParamsContainsCancel() {
        this.spec = new ObserveSpec.Builder().cancel().build();
        Assert.assertEquals("cancel", this.spec.toQueryParams()[0]);
    }
}
