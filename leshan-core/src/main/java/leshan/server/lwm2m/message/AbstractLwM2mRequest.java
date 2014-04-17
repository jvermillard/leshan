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

public abstract class AbstractLwM2mRequest implements LwM2mRequest {

    private final Integer objectId;
    private final Integer objectInstanceId;
    private final Integer resourceId;
    private final Client client;

    protected AbstractLwM2mRequest(Client client, Integer objectId, Integer objectInstanceId, Integer resourceId) {
        if (client == null) {
            throw new NullPointerException("Client must not be null");
        } else if (objectId == null) {
            throw new NullPointerException("Object ID must not be null");
        }
        this.client = client;
        this.objectId = objectId;
        this.objectInstanceId = objectInstanceId;
        this.resourceId = resourceId;
    }

    public Client getClient() {
        return this.client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Integer getObjectId() {
        return this.objectId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Integer getObjectInstanceId() {
        return this.objectInstanceId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Integer getResourceId() {
        return this.resourceId;
    }

}