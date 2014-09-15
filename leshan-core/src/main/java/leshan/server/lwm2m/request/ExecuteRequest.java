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
import leshan.server.lwm2m.node.LwM2mPath;

/**
 * A request for executing resources on a client.
 */
public class ExecuteRequest extends AbstractLwM2mRequest<ClientResponse> {

    private final byte[] parameters;
    private final ContentFormat contentFormat;

    public ExecuteRequest(Client client, String path) {
        this(client, new LwM2mPath(path), null, null);
    }

    public ExecuteRequest(Client client, String path, byte[] parameters, ContentFormat format) {
        this(client, new LwM2mPath(path), parameters, format);
    }

    /**
     * Creates a new <em>execute</em> request for a resource that does not require any parameters.
     * 
     * @param client the LWM2M Client to execute the resource on
     * @param objectId the resource's object ID
     * @param objectInstanceId the resource's object instance ID
     * @param resourceId the resource's ID
     */
    public ExecuteRequest(Client client, int objectId, int objectInstanceId, int resourceId) {
        this(client, new LwM2mPath(objectId, objectInstanceId, resourceId), null, null);
    }

    /**
     * Creates a new <em>execute</em> request for a resource accepting parameters encoded as plain text or JSON.
     * 
     * @param client the LWM2M Client to execute the resource on
     * @param objectId the resource's object ID
     * @param objectInstanceId the resource's object instance ID
     * @param resourceId the resource's ID
     * @param parameters the parameters
     */
    public ExecuteRequest(Client client, int objectId, int objectInstanceId, int resourceId, byte[] parameters,
            ContentFormat format) {
        this(client, new LwM2mPath(objectId, objectInstanceId, resourceId), parameters, format);
    }

    private ExecuteRequest(Client client, LwM2mPath path, byte[] parameters, ContentFormat format) {
        super(client, path);

        this.parameters = parameters;
        this.contentFormat = format;
    }

    @Override
    public String toString() {
        return String.format("ExecuteRequest [%s]", getPath());
    }

    @Override
    public void accept(LwM2mRequestVisitor visitor) {
        visitor.visit(this);
    }

    public byte[] getParameters() {
        return parameters;
    }

    public ContentFormat getContentFormat() {
        return contentFormat;
    }

}
