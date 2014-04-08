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
package leshan.server.lwm2m.message;

import java.io.UnsupportedEncodingException;

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvEncoder;

/**
 * A Lightweight M2M request supporting the submission of data with the request.
 */
public abstract class PayloadRequest extends AbstractLwM2mRequest {

    private final Tlv[] payload;
    private final String stringPayload;
    private final byte[] bytePayload;
    private final ContentFormat contentFormat;

    /**
     * Initializes all fields.
     * 
     * @param objectId
     * @param objectInstanceId
     * @param resourceId
     * @param payload
     * @param format
     * @param tlv
     * @param bytes
     */
    private PayloadRequest(Client client, Integer objectId, Integer objectInstanceId, Integer resourceId,
            String payload, ContentFormat format, Tlv[] tlv, byte[] bytes) {
        super(client, objectId, objectInstanceId, resourceId);
        this.stringPayload = payload;
        this.payload = tlv;
        this.bytePayload = bytes;
        this.contentFormat = format;

    }

    /**
     * Payload request with string payload.
     * 
     * @param client
     * @param objectId
     * @param objectInstanceId
     * @param resourceId
     * @param payload
     * @param format the payload format (JSON or TEXT)
     */
    protected PayloadRequest(Client client, Integer objectId, Integer objectInstanceId, Integer resourceId,
            String payload, ContentFormat format) {
        this(client, objectId, objectInstanceId, resourceId, payload, format, null, null);
        if (payload != null && !ContentFormat.TEXT.equals(format) && !ContentFormat.JSON.equals(format)) {
            throw new IllegalArgumentException("Content format must be either TEXT or JSON for string payload");
        }
    }

    /**
     * Payload request with TLV payload
     * 
     * @param client
     * @param objectId
     * @param objectInstanceId
     * @param resourceId
     * @param payload
     */
    protected PayloadRequest(Client client, Integer objectId, Integer objectInstanceId, Integer resourceId,
            Tlv[] payload) {
        this(client, objectId, objectInstanceId, resourceId, null, payload != null ? ContentFormat.TLV : null, payload,
                null);
    }

    /**
     * Payload request with binary payload.
     * 
     * @param client
     * @param objectId
     * @param objectInstanceId
     * @param resourceId
     * @param payload
     */
    protected PayloadRequest(Client client, Integer objectId, Integer objectInstanceId, Integer resourceId,
            byte[] payload) {
        this(client, objectId, objectInstanceId, resourceId, null, payload != null ? ContentFormat.OPAQUE : null, null,
                payload);
    }

    public final Tlv[] getPayload() {
        return this.payload;
    }

    public final String getStringPayload() {
        return this.stringPayload;
    }

    public final byte[] getBytes() {
        if (contentFormat == null) {
            return null;
        }

        switch (this.contentFormat) {
        case TLV:
            TlvEncoder encoder = new TlvEncoder();
            return encoder.encode(getPayload()).array();
        case OPAQUE:
            return this.bytePayload;
        default:
            // TEXT or JSON
            if (getStringPayload() != null) {
                try {
                    return getStringPayload().getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // can be safely ignored since Java by definition supports
                    // UTF-8 encoding
                }
            }
            return null;
        }
    }

    public final ContentFormat getContentFormat() {
        return this.contentFormat;
    }

}
