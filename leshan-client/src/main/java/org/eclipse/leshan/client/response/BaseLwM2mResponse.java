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
package org.eclipse.leshan.client.response;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.tlv.Tlv;
import org.eclipse.leshan.tlv.Tlv.TlvType;

public abstract class BaseLwM2mResponse implements LwM2mResponse {

    private final ResponseCode code;
    private final byte[] payload;

    public BaseLwM2mResponse(final ResponseCode code, final byte[] payload) {
        this.code = code;
        this.payload = payload;
    }

    @Override
    public ResponseCode getCode() {
        return code;
    }

    @Override
    public byte[] getResponsePayload() {
        return payload;
    }

    @Override
    public Tlv getResponsePayloadAsTlv() {
        return new Tlv(TlvType.RESOURCE_VALUE, null, payload, 0);
    }

    @Override
    public boolean isSuccess() {
        switch (code) {
        case CHANGED:
        case CONTENT:
        case CREATED:
        case DELETED:
            return true;
        case BAD_REQUEST:
        case CONFLICT:
        case METHOD_NOT_ALLOWED:
        case NOT_FOUND:
        case UNAUTHORIZED:
        default:
            return false;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof BaseLwM2mResponse)) {
            return false;
        }
        final BaseLwM2mResponse other = (BaseLwM2mResponse) o;
        return code == other.code && Arrays.equals(payload, other.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, Arrays.hashCode(payload));
    }

    @Override
    public String toString() {
        final String payloadString = (payload == null) ? "" : ", \"" + Arrays.toString(payload) + "\"";
        return "[" + getClass().getSimpleName() + ": " + code + payloadString + "]";
    }

}
