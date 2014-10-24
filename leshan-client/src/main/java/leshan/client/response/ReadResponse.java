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
package leshan.client.response;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import leshan.ResponseCode;
import leshan.tlv.Tlv;
import leshan.tlv.Tlv.TlvType;
import leshan.tlv.TlvDecoder;
import leshan.tlv.TlvEncoder;

public class ReadResponse extends BaseLwM2mResponse {

    private ReadResponse(final ResponseCode code, final byte[] payload) {
        super(code, payload);
    }

    private ReadResponse(final ResponseCode code) {
        this(code, new byte[0]);
    }

    public static ReadResponse success(final byte[] readValue) {
        return new ReadResponse(ResponseCode.CONTENT, readValue);
    }

    public static ReadResponse successMultiple(final Map<Integer, byte[]> readValues) {
        return new MultipleReadResponse(ResponseCode.CONTENT, readValues);
    }

    // TODO Evaluate whether this needs to be used
    public static ReadResponse failure() {
        return new ReadResponse(ResponseCode.METHOD_NOT_ALLOWED);
    }

    public static ReadResponse notAllowed() {
        return new ReadResponse(ResponseCode.METHOD_NOT_ALLOWED);
    }

    private static class MultipleReadResponse extends ReadResponse {

        private final Tlv tlvPayload;

        public MultipleReadResponse(final ResponseCode code, final Map<Integer, byte[]> readValues) {
            super(code, getPayload(readValues));
            tlvPayload = new Tlv(TlvType.MULTIPLE_RESOURCE, TlvDecoder.decode(ByteBuffer.wrap(getResponsePayload())),
                    null, 0);
        }

        @Override
        public Tlv getResponsePayloadAsTlv() {
            return tlvPayload;
        }

    }

    private static byte[] getPayload(final Map<Integer, byte[]> readValues) {
        final List<Tlv> children = new ArrayList<Tlv>();
        for (final Entry<Integer, byte[]> entry : new TreeMap<>(readValues).entrySet()) {
            children.add(new Tlv(TlvType.RESOURCE_INSTANCE, null, entry.getValue(), entry.getKey()));
        }
        return TlvEncoder.encode(children.toArray(new Tlv[0])).array();
    }

}
