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
package leshan.client.lwm2m.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import leshan.client.lwm2m.response.CreateResponse;

import org.junit.Test;

public class CreateResponseTest {

    @Test
    public void testEqualityRobustnessForSuccesses() {
        assertEquals(CreateResponse.success(1), CreateResponse.success(1));
        assertNotEquals(CreateResponse.success(1), CreateResponse.success(2));
        assertNotEquals(CreateResponse.success(2), CreateResponse.success(1));
        assertNotEquals(CreateResponse.success(1), CreateResponse.invalidResource());
        assertNotEquals(CreateResponse.success(1), null);
    }

    @Test
    public void testHashCodeRobustnessForSuccesses() {
        assertEquals(CreateResponse.success(1).hashCode(), CreateResponse.success(1).hashCode());
        assertNotEquals(CreateResponse.success(1).hashCode(), CreateResponse.success(2).hashCode());
        assertNotEquals(CreateResponse.success(1).hashCode(), CreateResponse.invalidResource().hashCode());
    }

    @Test
    public void testEqualityRobustnessForFailures() {
        assertEquals(CreateResponse.invalidResource(), CreateResponse.invalidResource());
        assertNotEquals(CreateResponse.invalidResource(), CreateResponse.success(2));
        assertNotEquals(CreateResponse.invalidResource(), null);
    }

    @Test
    public void testHashCodeRobustnessForFailures() {
        assertEquals(CreateResponse.invalidResource().hashCode(), CreateResponse.invalidResource().hashCode());
        assertNotEquals(CreateResponse.invalidResource(), CreateResponse.success(2).hashCode());
    }

}
