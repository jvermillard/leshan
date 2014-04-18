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
import leshan.server.lwm2m.observation.ResourceObserver;

public class ObserveRequest extends AbstractLwM2mRequest implements LwM2mRequest<ObserveResponse> {

    private final ResourceObserver observer;

    private ObserveRequest(ResourceSpec target, ResourceObserver observer) {
        super(target);
        if (observer == null) {
            throw new NullPointerException("Observer must not be null");
        }
        this.observer = observer;
    }

    /**
     * Creates a request for observing future changes of all instances of a
     * particular object of a client.
     * 
     * @param client the LWM2M Client to observe the resource of
     * @param observer the object to notify about future changes
     * @param objectId the object ID of the resource
     * @return the request object
     * @throws NullPointerException if the object ID is <code>null</code>
     */
    public static final ObserveRequest newRequest(Client client, ResourceObserver observer, Integer objectId) {
        return new ObserveRequest(new ResourceSpec(client, objectId, null, null), observer);
    }

    /**
     * Creates a request for observing future changes of a particular object
     * instance of a client.
     * 
     * @param client the LWM2M Client to observe the resource of
     * @param observer the object to notify about future changes
     * @param objectId the object ID of the resource
     * @param objectInstanceId the object instance ID
     * @return the request object
     * @throws NullPointerException if the object ID is <code>null</code>
     */
    public static final ObserveRequest newRequest(Client client, ResourceObserver observer, Integer objectId,
                                                  Integer objectInstanceId) {
        return new ObserveRequest(new ResourceSpec(client, objectId, objectInstanceId, null), observer);
    }

    /**
     * Creates a request for observing future changes of a specific resource of
     * a client.
     * 
     * @param client the LWM2M Client to observe the resource of
     * @param observer the object to notify about future changes
     * @param objectId the object ID of the resource
     * @param objectInstanceId the object instance ID
     * @param resourceId the (individual) resource's ID
     * @return the request object
     * @throws NullPointerException if the object ID is <code>null</code>
     */
    public static final ObserveRequest newRequest(Client client, ResourceObserver observer, Integer objectId,
                                                  Integer objectInstanceId,
                                                  Integer resourceId) {
        return new ObserveRequest(new ResourceSpec(client, objectId, objectInstanceId, resourceId), observer);
    }

    @Override
    public ObserveResponse send(RequestHandler operations) {
        return operations.send(this);
    }

    public ResourceObserver getObserver() {
        return this.observer;
    }

    @Override
    public final String toString() {
        return String.format("ObserveRequest [%s]", getTarget());
    }

}
