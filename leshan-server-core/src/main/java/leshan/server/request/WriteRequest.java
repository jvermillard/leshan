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

import leshan.core.node.LwM2mNode;
import leshan.core.node.LwM2mPath;
import leshan.core.objectspec.ResourceSpec;
import leshan.core.objectspec.Resources;
import leshan.core.request.ContentFormat;
import leshan.core.response.ClientResponse;
import leshan.server.client.Client;
import leshan.util.Validate;

/**
 * The request to change the value of a Resource, an array of Resources Instances or multiple Resources from an Object
 * Instance.
 */
public class WriteRequest extends AbstractLwM2mRequest<ClientResponse> {

    private final LwM2mNode node;
    private final ContentFormat contentFormat;

    private final boolean replaceRequest;

    public WriteRequest(final Client client, final int objectId, final int objectInstanceId, final int resourceId,
            final LwM2mNode node, final ContentFormat contentFormat, final boolean replaceResources) {
        this(client, new LwM2mPath(objectId, objectInstanceId, resourceId), node, contentFormat, replaceResources);
    }

    public WriteRequest(final Client client, final String target, final LwM2mNode node,
            final ContentFormat contentFormat, final boolean replaceResources) {
        this(client, new LwM2mPath(target), node, contentFormat, replaceResources);
    }

    private WriteRequest(final Client client, final LwM2mPath target, final LwM2mNode node, ContentFormat format,
            final boolean replaceResources) {
        super(client, target);
        Validate.notNull(node);

        // Manage Text format
        if (ContentFormat.TEXT == format) {
            if (!getPath().isResource()) {
                throw new IllegalArgumentException("Text format must be used only for single resources");
            } else {
                final ResourceSpec description = Resources.getResourceSpec(getPath().getObjectId(), getPath()
                        .getResourceId());
                if (description != null && description.multiple) {
                    throw new IllegalArgumentException("Text format must be used only for single resources");
                }
            }
        }

        // Manage default format
        if (format == null) {
            // use text for single resource
            if (getPath().isResource()) {
                final ResourceSpec description = Resources.getResourceSpec(getPath().getObjectId(), getPath()
                        .getResourceId());
                if (description != null && !description.multiple) {
                    format = ContentFormat.TEXT;
                } else {
                    format = ContentFormat.TLV;
                }
            } else {
                format = ContentFormat.TLV;
            }
        }

        this.node = node;
        this.contentFormat = format;
        this.replaceRequest = replaceResources;
    }

    /**
     * Checks whether this write request is supposed to replace all resources or do a partial update only (see section
     * 5.3.3 of the LW M2M spec).
     *
     * @return <code>true</code> if all resources are to be replaced
     */
    public boolean isReplaceRequest() {
        return this.replaceRequest;
    }

    public LwM2mNode getNode() {
        return node;
    }

    public ContentFormat getContentFormat() {
        return contentFormat;
    }

    @Override
    public void accept(final LwM2mRequestVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("WriteRequest [replaceRequest=%s, getPath()=%s]", replaceRequest, getPath());
    }

}