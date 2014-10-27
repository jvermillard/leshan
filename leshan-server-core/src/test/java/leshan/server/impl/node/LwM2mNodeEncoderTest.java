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
package leshan.server.impl.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import leshan.server.impl.objectspec.Resources;
import leshan.server.node.LwM2mObjectInstance;
import leshan.server.node.LwM2mPath;
import leshan.server.node.LwM2mResource;
import leshan.server.node.Value;
import leshan.server.request.ContentFormat;

import org.apache.commons.io.Charsets;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for {@link LwM2mNodeEncoder}
 */
public class LwM2mNodeEncoderTest {

    @BeforeClass
    public static void loadResourceSpec() {
        Resources.load();
    }

    @Test
    public void text_encode_single_resource() {

        byte[] encoded = LwM2mNodeEncoder.encode(new LwM2mResource(15, Value.newDoubleValue(56.4D)),
                ContentFormat.TEXT, new LwM2mPath("/323/0/15"));

        Assert.assertEquals("56.4", new String(encoded, Charsets.UTF_8));
    }

    @Test
    public void text_encode_date_as_long() {

        byte[] encoded = LwM2mNodeEncoder.encode(
                new LwM2mResource(13, Value.newStringValue("2010-01-01T12:00:00+01:00")), ContentFormat.TEXT,
                new LwM2mPath("/3/0/13"));

        Assert.assertEquals("1262343600", new String(encoded, Charsets.UTF_8));
    }

    @Test
    public void text_encode_date_as_iso_string() {

        byte[] encoded = LwM2mNodeEncoder.encode(new LwM2mResource(13, Value.newLongValue(1367491215000L)),
                ContentFormat.TEXT, new LwM2mPath("/3/0/13"));

        Assert.assertEquals("1367491215", new String(encoded, Charsets.UTF_8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void text_encode_multiple_instances() {
        LwM2mNodeEncoder.encode(
                new LwM2mResource(6, new Value[] { Value.newIntegerValue(1), Value.newIntegerValue(5) }),
                ContentFormat.TEXT, new LwM2mPath("/3/0/6"));
    }

    @Test
    public void tlv_encode_device_object_instance() {

        Collection<LwM2mResource> resources = new ArrayList<>();

        resources.add(new LwM2mResource(0, Value.newStringValue("Open Mobile Alliance")));
        resources.add(new LwM2mResource(1, Value.newStringValue("Lightweight M2M Client")));
        resources.add(new LwM2mResource(2, Value.newStringValue("345000123")));
        resources.add(new LwM2mResource(3, Value.newStringValue("1.0")));

        resources.add(new LwM2mResource(6, new Value[] { Value.newIntegerValue(1), Value.newIntegerValue(5) }));
        resources.add(new LwM2mResource(7, new Value[] { Value.newIntegerValue(3800), Value.newIntegerValue(5000) }));
        resources.add(new LwM2mResource(8, new Value[] { Value.newIntegerValue(125), Value.newIntegerValue(900) }));
        resources.add(new LwM2mResource(9, Value.newIntegerValue(100)));
        resources.add(new LwM2mResource(10, Value.newIntegerValue(15)));
        resources.add(new LwM2mResource(11, Value.newIntegerValue(0)));
        resources.add(new LwM2mResource(13, Value.newDateValue(new Date(1367491215000L))));
        resources.add(new LwM2mResource(14, Value.newStringValue("+02:00")));
        resources.add(new LwM2mResource(15, Value.newStringValue("U")));

        LwM2mObjectInstance oInstance = new LwM2mObjectInstance(0, resources.toArray(new LwM2mResource[0]));

        byte[] encoded = LwM2mNodeEncoder.encode(oInstance, ContentFormat.TLV, new LwM2mPath("/3/0"));

        // tlv content for instance 0 of device object
        byte[] expected = new byte[] { -56, 0, 20, 79, 112, 101, 110, 32, 77, 111, 98, 105, 108, 101, 32, 65, 108, 108,
                                105, 97, 110, 99, 101, -56, 1, 22, 76, 105, 103, 104, 116, 119, 101, 105, 103, 104,
                                116, 32, 77, 50, 77, 32, 67, 108, 105, 101, 110, 116, -56, 2, 9, 51, 52, 53, 48, 48,
                                48, 49, 50, 51, -61, 3, 49, 46, 48, -122, 6, 65, 0, 1, 65, 1, 5, -120, 7, 8, 66, 0, 14,
                                -40, 66, 1, 19, -120, -121, 8, 65, 0, 125, 66, 1, 3, -124, -63, 9, 100, -63, 10, 15,
                                -63, 11, 0, -60, 13, 81, -126, 66, -113, -58, 14, 43, 48, 50, 58, 48, 48, -63, 15, 85 };

        Assert.assertArrayEquals(expected, encoded);
    }
}
