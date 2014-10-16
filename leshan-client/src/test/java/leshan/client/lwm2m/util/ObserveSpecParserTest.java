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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import leshan.server.lwm2m.observation.ObserveSpec;

import org.junit.Test;

public class ObserveSpecParserTest {

	@Test(expected=IllegalArgumentException.class)
	public void testInvalidFormat() {
		ObserveSpecParser.parse(Arrays.asList("a=b=c"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testInvalidKey() {
		ObserveSpecParser.parse(Arrays.asList("a=b"));
	}

	@Test
	public void testCancel() {
		testCorrectSpec(new ObserveSpec.Builder().cancel().build(),
				"cancel");
	}

	@Test
	public void testGreaterThan6() {
		testCorrectSpec(new ObserveSpec.Builder().greaterThan(6).build(),
				"gt=6.0");
	}

	@Test
	public void testGreaterThan8() {
		testCorrectSpec(new ObserveSpec.Builder().greaterThan(8).build(),
				"gt=8.0");
	}

	@Test
	public void testLessThan8() {
		testCorrectSpec(new ObserveSpec.Builder().lessThan(8).build(),
				"lt=8.0");
	}

	@Test
	public void testLessThan8AndGreaterThan14() {
		testCorrectSpec(new ObserveSpec.Builder().greaterThan(14).lessThan(8).build(),
				"lt=8.0",
				"gt=14.0");
	}

	@Test
	public void testAllTheThings() {
		final ObserveSpec spec = new ObserveSpec.Builder()
				.greaterThan(14)
				.lessThan(8)
				.minPeriod(5)
				.maxPeriod(10)
				.step(1)
				.build();
		testCorrectSpec(spec,
				"gt=14.0",
				"lt=8.0",
				"pmin=5",
				"pmax=10",
				"st=1.0");
	}

	@Test(expected=IllegalStateException.class)
	public void testOutOfOrderPminPmax() {
		ObserveSpecParser.parse(Arrays.asList("pmin=50", "pmax=10"));
	}

	private void testCorrectSpec(final ObserveSpec expected, final String... inputs) {
		final List<String> queries = Arrays.asList(inputs);
		final ObserveSpec actual = ObserveSpecParser.parse(queries);
		assertSameSpecs(expected, actual);
	}

	private void assertSameSpecs(final ObserveSpec expected, final ObserveSpec actual) {
		assertEquals(expected.toString(), actual.toString());
	}

}
