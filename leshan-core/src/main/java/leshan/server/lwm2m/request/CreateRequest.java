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
package leshan.server.lwm2m.request;

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.node.LwM2mNode;
import leshan.server.lwm2m.node.LwM2mObjectInstance;
import leshan.server.lwm2m.node.LwM2mPath;

/**
 * A Lightweight M2M request for creating resources on a client.
 */
public class CreateRequest extends AbstractLwM2mRequest<ClientResponse> {

    private final LwM2mObjectInstance instance;

    private final ContentFormat contentFormat;

    /**
     * Creates a request for creating the (only) instance of a particular object.
     * 
     * @param client the LWM2M Client to create the object instance on
     * @param objectId the object ID
     * @param values the TLV encoded resource values of the object instance
     */
    public CreateRequest(Client client, int objectId, LwM2mObjectInstance instance, ContentFormat contentFormat) {
        this(client, new LwM2mPath(objectId), instance, contentFormat);
    }

    /**
     * Creates a request for creating the (only) instance of a particular object.
     * 
     * @param client the LWM2M Client to create the object instance on
     * @param objectId the object ID
     * @param objectInstanceId the ID of the new object instance
     * @param values the TLV encoded resource values of the object instance
     */
    public CreateRequest(Client client, int objectId, int objectInstanceId, LwM2mObjectInstance instance,
            ContentFormat contentFormat) {
        this(client, new LwM2mPath(objectId, objectInstanceId), instance, contentFormat);
    }

    /**
     * Creates a request for creating the (only) instance of a particular object.
     * 
     * @param client the LWM2M Client to create the object instance on
     * @param path the target path
     * @param values the TLV encoded resource values of the object instance
     */
    public CreateRequest(Client client, String path, LwM2mObjectInstance instance, ContentFormat contentFormat) {
        this(client, new LwM2mPath(path), instance, contentFormat);
    }

    private CreateRequest(Client client, LwM2mPath target, LwM2mObjectInstance instance, ContentFormat format) {
        super(client, target);

        if (target.isResource()) {
            throw new IllegalArgumentException("Cannot create a resource node");
        }

        this.instance = instance;
        this.contentFormat = format != null ? format : ContentFormat.TLV; // default to TLV
    }

    @Override
    public void accept(LwM2mRequestVisitor visitor) {
        visitor.visit(this);
    }

    public LwM2mNode getObjectInstance() {
        return instance;
    }

    public ContentFormat getContentFormat() {
        return contentFormat;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CreateRequest [").append(getPath()).append("]");
        return builder.toString();
    }
}
