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

public final class ResourceSpec {

    private final Client client;
    private final Integer objectId;
    private final Integer objectInstanceId;
    private final Integer resourceId;
    private String stringRepresentation;

    public ResourceSpec(Client client, Integer objectId, Integer objectInstanceId, Integer resourceId) {
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

    public Integer getObjectId() {
        return this.objectId;
    }

    public Integer getObjectInstanceId() {
        return this.objectInstanceId;
    }

    public Integer getResourceId() {
        return this.resourceId;
    }

    public String asRelativePath() {
        StringBuffer b = new StringBuffer();
        b.append("/");
        b.append(getObjectId());
        if (getObjectInstanceId() != null) {
            b.append("/").append(getObjectInstanceId());
            if (getResourceId() != null) {
                b.append("/").append(getResourceId());
            }
        }
        return b.toString();
    }

    @Override
    public String toString() {
        synchronized (this) {
            if (this.stringRepresentation == null) {
                StringBuilder b = new StringBuilder("client=");
                b.append(getClient().getEndpoint());
                b.append(", objectId=").append(getObjectId());
                b.append(", objectInstanceId=").append(getObjectInstanceId());
                b.append(", resourceId=").append(getResourceId());
                this.stringRepresentation = b.toString();
            }
        }
        return this.stringRepresentation;
    }
}
