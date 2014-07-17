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
package leshan.server.lwm2m.linkformat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import leshan.server.lwm2m.client.LinkObject;

import org.junit.Assert;
import org.junit.Test;

public class LinkFormatParserTest {

    @Test
    public void parse_with_some_attributes() {
        LinkObject[] parse = LinkFormatParser
                .parse("</>;rt=\"oma.lwm2m\";ct=100, </1/101>,</1/102>, </2/0>, </2/1> ;empty".getBytes());
        Assert.assertEquals(5, parse.length);
        Assert.assertEquals("/", parse[0].getUrl());

        Map<String, String> attResult = new HashMap<>();
        attResult.put("rt", "\"oma.lwm2m\"");
        attResult.put("ct", "100");
        Assert.assertEquals(attResult, parse[0].getAttributes());

        Assert.assertEquals("/1/101", parse[1].getUrl());
        Assert.assertEquals(Collections.EMPTY_MAP, parse[1].getAttributes());
        Assert.assertEquals("/1/102", parse[2].getUrl());
        Assert.assertEquals(Collections.EMPTY_MAP, parse[2].getAttributes());
        Assert.assertEquals("/2/0", parse[3].getUrl());
        Assert.assertEquals(Collections.EMPTY_MAP, parse[3].getAttributes());
        Assert.assertEquals("/2/1", parse[4].getUrl());

        attResult = new HashMap<>();
        attResult.put("empty", null);
        Assert.assertEquals(attResult, parse[4].getAttributes());
    }
}
