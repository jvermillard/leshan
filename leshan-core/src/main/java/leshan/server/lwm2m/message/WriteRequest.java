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

import java.util.Arrays;

import leshan.server.lwm2m.tlv.Tlv;

import org.apache.commons.lang.Validate;

/**
 * The request to change the value of a Resource, an array of Resources Instances or multiple Resources from an Object
 * Instance.
 */
public class WriteRequest {

    private final Integer objectId;

    private final Integer objectInstanceId;

    private final Integer resourceId;

    private final ContentFormat format;

    /** value for text and json content */
    private final String stringValue;

    /** value for TLV content */
    private final Tlv[] tlvValues;

    public WriteRequest(Integer objectId, Integer objectInstanceId, Integer resourceId, ContentFormat format,
            String stringValue, Tlv[] tlvValues) {
        Validate.notNull(objectId);
        Validate.notNull(objectInstanceId);
        Validate.notNull(format);

        switch (format) {
        case TEXT:
            Validate.notNull(resourceId);
        case JSON:
            Validate.notNull(stringValue);
            if (tlvValues != null) {
                throw new IllegalArgumentException("a value with format " + format + " cannot contain Tlv values");
            }
            break;
        case TLV:
            Validate.notNull(tlvValues);
            if (stringValue != null) {
                throw new IllegalArgumentException("a value with format TLV cannot contain a String value");
            }
        default:
            throw new IllegalArgumentException("unsupported content format for write request : " + format);
        }

        this.objectId = objectId;
        this.objectInstanceId = objectInstanceId;
        this.resourceId = resourceId;
        this.format = format;
        this.stringValue = stringValue;
        this.tlvValues = tlvValues;

    }

    public Integer getObjectId() {
        return objectId;
    }

    public Integer getObjectInstanceId() {
        return objectInstanceId;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public ContentFormat getFormat() {
        return format;
    }

    public String getStringValue() {
        return stringValue;
    }

    public Tlv[] getTlvValues() {
        return tlvValues;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WriteRequest [objectId=").append(objectId).append(", objectInstanceId=")
                .append(objectInstanceId).append(", resourceId=").append(resourceId).append(", format=").append(format)
                .append(", stringValue=").append(stringValue).append(", tlvValues=").append(Arrays.toString(tlvValues))
                .append("]");
        return builder.toString();
    }

}
