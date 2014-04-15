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
 * A Lightweight M2M request for creating resources on a client.
 */
public class CreateRequest extends PayloadRequest {

    private CreateRequest(Client client, Integer objectId, Integer objectInstanceId, String payload,
            ContentFormat contentFormat) {
        super(client, objectId, objectInstanceId, null, payload, contentFormat);
    }

    private CreateRequest(Client client, Integer objectId, Integer objectInstanceId, Tlv[] payload) {
        super(client, objectId, objectInstanceId, null, payload);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientResponse send(RequestHandler operations) {
        return operations.send(this);
    }

    /**
     * Creates a request for creating the (only) instance of a particular object.
     * 
     * @param client the LWM2M Client to create the object instance on
     * @param objectId the object ID
     * @param values the TLV encoded resource values of the object instance
     * @return the request object
     */
    public static CreateRequest newRequest(Client client, Integer objectId, Tlv[] values) {
        return CreateRequest.newRequest(client, objectId, null, values);
    }

    /**
     * Creates a request for creating the (only) instance of a particular object.
     * 
     * @param client the LWM2M Client to create the object instance on
     * @param objectId the object ID
     * @param objectInstanceId the ID of the new object instance
     * @param values the TLV encoded resource values of the object instance
     * @return the request object
     */
    public static CreateRequest newRequest(Client client, Integer objectId, Integer objectInstanceId, Tlv[] values) {
        return new CreateRequest(client, objectId, null, values);
    }

    /**
     * Creates a request for creating the (only) instance of a particular object.
     * 
     * @param client the LWM2M Client to create the object instance on
     * @param objectId the object ID
     * @param values the JSON encoded resource values of the object instance
     * @return the request object
     */
    public static CreateRequest newRequest(Client client, Integer objectId, String values) {
        return CreateRequest.newRequest(client, objectId, null, values);
    }

    /**
     * Creates a request for creating an additional instance of a particular object.
     * 
     * @param client the LWM2M Client to create the object instance on
     * @param objectId the object ID
     * @param objectInstanceId the ID of the new object instance
     * @param values the JSON encoded resource values of the object instance
     * @return the request object
     */
    public static CreateRequest newRequest(Client client, Integer objectId, Integer objectInstanceId, String values) {
        return new CreateRequest(client, objectId, null, values, ContentFormat.JSON);
    }

}