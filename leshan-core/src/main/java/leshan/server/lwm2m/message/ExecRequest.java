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

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.tlv.Tlv;

/**
 * A request for executing resources on a client.
 */
public class ExecRequest extends PayloadRequest {

    private ExecRequest(Client client, Integer objectId, Integer objectInstanceId, Integer resourceId, Tlv[] payload) {
        super(client, objectId, objectInstanceId, resourceId, payload);
    }

    private ExecRequest(Client client, Integer objectId, Integer objectInstanceId, Integer resourceId, byte[] payload) {
        super(client, objectId, objectInstanceId, resourceId, payload);
    }

    private ExecRequest(Client client, Integer objectId, Integer objectInstanceId, Integer resourceId, String payload,
            ContentFormat format) {
        super(client, objectId, objectInstanceId, resourceId, payload, format);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ExecRequest [client=").append(getClient().getEndpoint()).append(", objectId=")
                .append(getObjectId()).append(", objectInstanceId=").append(getObjectInstanceId())
                .append(", resourceId=").append(getResourceId()).append("]");
        return builder.toString();
    }

    /**
     * Creates a new <em>execute</em> request for a resource that does not require any parameters.
     * 
     * @param client the LWM2M Client to execute the resource on
     * @param objectId the resource's object ID
     * @param objectInstanceId the resource's object instance ID
     * @param resourceId the resource's ID
     * @return the request object
     */
    public static ExecRequest newRequest(Client client, Integer objectId, Integer objectInstanceId, Integer resourceId) {
        return new ExecRequest(client, objectId, objectInstanceId, resourceId, null, null);
    }

    /**
     * Creates a new <em>execute</em> request for a resource accepting TLV encoded parameters.
     * 
     * @param client the LWM2M Client to execute the resource on
     * @param objectId the resource's object ID
     * @param objectInstanceId the resource's object instance ID
     * @param resourceId the resource's ID
     * @param parameters the parameters
     * @return the request object
     */
    public static ExecRequest newRequest(Client client, Integer objectId, Integer objectInstanceId, Integer resourceId,
            Tlv[] parameters) {
        return new ExecRequest(client, objectId, objectInstanceId, resourceId, parameters);
    }

    /**
     * Creates a new <em>execute</em> request for a resource accepting parameters encoded as plain text or JSON.
     * 
     * @param client the LWM2M Client to execute the resource on
     * @param objectId the resource's object ID
     * @param objectInstanceId the resource's object instance ID
     * @param resourceId the resource's ID
     * @param parameters the parameters
     * @return the request object
     */
    public static ExecRequest newRequest(Client client, Integer objectId, Integer objectInstanceId, Integer resourceId,
            String parameters, ContentFormat format) {
        return new ExecRequest(client, objectId, objectInstanceId, resourceId, parameters, format);
    }

    /**
     * Creates a new <em>execute</em> request for a resource accepting parameters encoded as an opaque byte array.
     * 
     * @param client the LWM2M Client to execute the resource on
     * @param objectId the resource's object ID
     * @param objectInstanceId the resource's object instance ID
     * @param resourceId the resource's ID
     * @param parameters the parameters
     * @return the request object
     */
    public static ExecRequest newRequest(Client client, Integer objectId, Integer objectInstanceId, Integer resourceId,
            byte[] parameters) {
        return new ExecRequest(client, objectId, objectInstanceId, resourceId, parameters);
    }

    @Override
    public ClientResponse send(LwM2mClientOperations operations) {
        return operations.send(this);
    }
}
