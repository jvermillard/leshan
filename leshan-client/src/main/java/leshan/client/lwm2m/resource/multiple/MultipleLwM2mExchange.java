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
package leshan.client.lwm2m.resource.multiple;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.TypedLwM2mExchange;
import leshan.client.lwm2m.response.ReadResponse;
import leshan.server.lwm2m.impl.tlv.Tlv;
import leshan.server.lwm2m.impl.tlv.Tlv.TlvType;
import leshan.server.lwm2m.impl.tlv.TlvDecoder;
import leshan.server.lwm2m.impl.tlv.TlvEncoder;

public class MultipleLwM2mExchange extends TypedLwM2mExchange<Map<Integer, byte[]>> {

    public MultipleLwM2mExchange(final LwM2mExchange exchange) {
        super(exchange);
    }

    @Override
    public void respondContent(final Map<Integer, byte[]> value) {
        advanced().respond(ReadResponse.successMultiple(value));
    }

    @Override
    protected Map<Integer, byte[]> convertFromBytes(final byte[] value) {
        final Tlv[] tlvs = TlvDecoder.decode(ByteBuffer.wrap(value));
        final Map<Integer, byte[]> result = new HashMap<>();
        for (final Tlv tlv : tlvs) {
            if (tlv.getType() != TlvType.RESOURCE_INSTANCE) {
                throw new IllegalArgumentException();
            }
            result.put(tlv.getIdentifier(), tlv.getValue());
        }
        return result;
    }

    @Override
    protected byte[] convertToBytes(final Map<Integer, byte[]> value) {
        final List<Tlv> tlvs = new ArrayList<>();
        for (final Entry<Integer, byte[]> entry : new TreeMap<Integer, byte[]>(value).entrySet()) {
            tlvs.add(new Tlv(TlvType.RESOURCE_INSTANCE, null, entry.getValue(), entry.getKey()));
        }
        return TlvEncoder.encode(tlvs.toArray(new Tlv[0])).array();
    }

}
