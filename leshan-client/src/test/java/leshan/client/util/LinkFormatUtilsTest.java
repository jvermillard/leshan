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
package leshan.client.util;

import static org.junit.Assert.assertEquals;
import leshan.LinkObject;

import org.junit.Test;

public class LinkFormatUtilsTest {
    private final String VALID_REQUEST_PAYLOAD = "</lwm2m>;rt=\"oma.lwm2m\", </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";
    private final String VALID_REQUEST_SIMPLE_PAYLOAD = "</1/101>, </1/102>, </2/0>, </2/1>, </2/2>, </3/0>, </4/0>, </5>";
    private final String INVALID_REQUEST_PAYLOAD = "";

    @Test
    public void testValidOne() {
        validateExpectedPayload(VALID_REQUEST_PAYLOAD);
    }

    @Test
    public void testValidTwo() {
        validateExpectedPayload(VALID_REQUEST_SIMPLE_PAYLOAD);
    }

    @Test
    public void testInvalid() {
        final LinkObject[] links = generateLinksFromPayload(INVALID_REQUEST_PAYLOAD);
        final String actualPayload = LinkFormatUtils.payloadize(links);

        assertEquals(LinkFormatUtils.INVALID_LINK_PAYLOAD, actualPayload);
    }

    private void validateExpectedPayload(final String expectedPayload) {
        final LinkObject[] links = generateLinksFromPayload(expectedPayload);
        final String actualPayload = LinkFormatUtils.payloadize(links);

        assertEquals(expectedPayload, actualPayload);
    }

    private LinkObject[] generateLinksFromPayload(final String payload) {
        return LinkObject.parse(payload.getBytes());
    }

}
