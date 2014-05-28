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

/**
 * A Lightweight M2M request.
 */
public interface LwM2mRequest {

    /**
     * Gets the LWM2M Client the request is targeted at.
     * 
     * @return the client
     */
    Client getClient();

    /**
     * Returns the object ID in the request path.
     * 
     * @return the object ID
     */
    Integer getObjectId();

    /**
     * Returns the object instance ID in the request path.
     * 
     * @return the object instance ID. Can be <code>null</code> when the request targets the whole object.
     */
    Integer getObjectInstanceId();

    /**
     * Returns the resource ID in the request path.
     * 
     * @return the resource ID. Can be <code>null</code> when the request targets the whole object/object instance.
     */
    Integer getResourceId();

    /**
     * Sends the request to the LWM2M Client.
     * 
     * @param handler the LWM2M protocol adapter to use for sending the request
     * @return the response from the client
     * @throws ResourceAccessException if the request could not be processed by the client
     */
    ClientResponse send(RequestHandler handler);
}
