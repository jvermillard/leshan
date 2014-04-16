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

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.operation.RequestHandler;
import leshan.server.lwm2m.tlv.Tlv;

/**
 * The request to change the value of a Resource, an array of Resources Instances or multiple Resources from an Object
 * Instance.
 */
public class WriteRequest extends PayloadRequest {

    private final boolean replaceRequest;

    protected WriteRequest(Client client, Integer objectId, Integer objectInstanceId, Integer resourceId,
            Tlv[] payload, boolean replaceResources) {
        super(client, objectId, objectInstanceId, resourceId, payload);
        if (payload == null) {
            throw new IllegalArgumentException("Payload must not be null");
        }
        this.replaceRequest = replaceResources;
    }

    protected WriteRequest(Client client, Integer objectId, Integer objectInstanceId, Integer resourceId,
            String payload, ContentFormat format, boolean replaceResources) {
        super(client, objectId, objectInstanceId, resourceId, payload, format);
        if (payload == null) {
            throw new IllegalArgumentException("Payload must not be null");
        } else if (ContentFormat.TEXT.equals(format) && resourceId == null) {
            throw new IllegalArgumentException("Payload of type TEXT can only be written to specific resources");
        }
        this.replaceRequest = replaceResources;
    }

    protected WriteRequest(Client client, Integer objectId, Integer objectInstanceId, Integer resourceId,
            byte[] payload, boolean replaceResources) {
        super(client, objectId, objectInstanceId, resourceId, payload);
        if (payload == null) {
            throw new IllegalArgumentException("Payload must not be null");
        }
        this.replaceRequest = replaceResources;
    }

    /**
     * 
     * @return the content format
     * @deprecated Use {@link #getContentFormat()} instead
     */
    @Deprecated
    public ContentFormat getFormat() {
        return super.getContentFormat();
    }

    /**
     * 
     * @return the string payload
     * @deprecated Use {@link #getStringPayload()} instead
     */
    @Deprecated
    public String getStringValue() {
        return super.getStringPayload();
    }

    /**
     * 
     * @return the TLV payload
     * @deprecated Use {@link #getPayload()} instead
     */
    @Deprecated
    public Tlv[] getTlvValues() {
        return super.getPayload();
    }

    /**
     * Checks whether this write request is supposed to replace all resources or do a partial update only (see section
     * 5.3.3 of the LW M2M spec).
     * 
     * @return <code>true</code> if all resources are to be replaced
     */
    public boolean isReplaceRequest() {
        return this.replaceRequest;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WriteRequest [client=").append(getClient().getEndpoint()).append(", objectId=")
                .append(getObjectId()).append(", objectInstanceId=").append(getObjectInstanceId())
                .append(", resourceId=").append(getResourceId()).append(", format=").append(getContentFormat())
                .append(", stringValue=").append(getStringPayload()).append(", tlvValues=")
                .append(Arrays.toString(getPayload())).append("]");
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientResponse send(RequestHandler operations) {
        return operations.send(this);
    }

    public static WriteRequest newReplaceRequest(Client client, Integer objectId, Integer objectInstanceId,
            Integer resourceId, Tlv[] payload) {
        return new WriteRequest(client, objectId, objectInstanceId, resourceId, payload, true);
    }

    public static WriteRequest newUpdateRequest(Client client, Integer objectId, Integer objectInstanceId,
            Integer resourceId, Tlv[] payload) {
        return new WriteRequest(client, objectId, objectInstanceId, resourceId, payload, false);
    }

    public static WriteRequest newReplaceRequest(Client client, Integer objectId, Integer objectInstanceId,
            Integer resourceId, String payload, ContentFormat format) {
        return new WriteRequest(client, objectId, objectInstanceId, resourceId, payload, format, true);
    }

    public static WriteRequest newUpdateRequest(Client client, Integer objectId, Integer objectInstanceId,
            Integer resourceId, String payload, ContentFormat format) {
        return new WriteRequest(client, objectId, objectInstanceId, resourceId, payload, format, false);
    }
}
