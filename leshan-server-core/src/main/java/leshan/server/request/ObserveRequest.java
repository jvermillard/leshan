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
package leshan.server.request;

import leshan.core.node.LwM2mPath;
import leshan.core.response.ValueResponse;
import leshan.server.client.Client;

public class ObserveRequest extends AbstractLwM2mRequest<ValueResponse> {

    public ObserveRequest(Client client, String target) {
        super(client, new LwM2mPath(target));
    }

    /**
     * Creates a request for observing future changes of all instances of a particular object of a client.
     * 
     * @param client the LWM2M Client to observe the resource of
     * @param objectId the object ID of the resource
     */
    public ObserveRequest(Client client, int objectId) {
        super(client, new LwM2mPath(objectId));
    }

    /**
     * Creates a request for observing future changes of a particular object instance of a client.
     * 
     * @param client the LWM2M Client to observe the resource of
     * @param objectId the object ID of the resource
     * @param objectInstanceId the object instance ID
     */
    public ObserveRequest(Client client, int objectId, int objectInstanceId) {
        super(client, new LwM2mPath(objectId, objectInstanceId));
    }

    /**
     * Creates a request for observing future changes of a specific resource of a client.
     * 
     * @param client the LWM2M Client to observe the resource of
     * @param objectId the object ID of the resource
     * @param objectInstanceId the object instance ID
     * @param resourceId the (individual) resource's ID
     */
    public ObserveRequest(Client client, int objectId, int objectInstanceId, int resourceId) {
        super(client, new LwM2mPath(objectId, objectInstanceId, resourceId));
    }

    @Override
    public void accept(LwM2mRequestVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public final String toString() {
        return String.format("ObserveRequest [%s]", getPath());
    }

}
