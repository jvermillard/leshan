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

public class DiscoverRequest extends AbstractLwM2mRequest implements LwM2mRequest {

    private DiscoverRequest(ResourceSpec target) {
        super(target);
    }

    @Override
    public final String toString() {
        return String.format("DiscoverRequest [%s]", getTarget());
    }

    /**
     * Creates a request for discovering the resources implemented by a client for a particular object type.
     * 
     * @param client the LWM2M Client to discover resources for
     * @param objectId the object type
     * @return the request object
     * @throws NullPointerException if the object ID is <code>null</code>
     */
    public static final DiscoverRequest newRequest(Client client, Integer objectId) {
        return DiscoverRequest.newRequest(client, objectId, null, null);
    }

    /**
     * Creates a request for discovering the resources implemented by a client for a particular object instance.
     * 
     * @param client the LWM2M Client to discover resources for
     * @param objectId the object type
     * @param objectInstanceId the object instance
     * @return the request object
     * @throws NullPointerException if the object ID is <code>null</code>
     */
    public static final DiscoverRequest newRequest(Client client, Integer objectId, Integer objectInstanceId) {
        return DiscoverRequest.newRequest(client, objectId, objectInstanceId, null);
    }

    /**
     * Creates a request for discovering the attributes of a particular resource implemented by a client.
     * 
     * @param client the LWM2M Client to discover resources for
     * @param objectId the object type
     * @param objectInstanceId the object instance
     * @param resourceId the resource
     * @return the request object
     * @throws NullPointerException if the object ID is <code>null</code>
     */
    public static final DiscoverRequest newRequest(Client client, Integer objectId, Integer objectInstanceId,
            Integer resourceId) {
        return new DiscoverRequest(new ResourceSpec(client, objectId, objectInstanceId, resourceId));
    }

    @Override
    public ClientResponse send(RequestHandler operations) {

        return operations.send(this);
    }
}
