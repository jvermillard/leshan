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
package leshan.core.request;

import leshan.core.node.LwM2mNode;
import leshan.core.node.LwM2mObject;
import leshan.core.node.LwM2mObjectInstance;
import leshan.core.node.LwM2mPath;
import leshan.core.node.LwM2mResource;
import leshan.core.node.Value.DataType;
import leshan.core.objectspec.ResourceSpec;
import leshan.core.objectspec.ResourceSpec.Type;
import leshan.core.objectspec.Resources;
import leshan.core.response.LwM2mResponse;
import leshan.util.Validate;

/**
 * The request to change the value of a Resource, an array of Resources Instances or multiple Resources from an Object
 * Instance.
 */
public class WriteRequest extends AbstractDownlinkRequest<LwM2mResponse> {

    private final LwM2mNode node;
    private final ContentFormat contentFormat;

    private final boolean replaceRequest;

    public WriteRequest(final int objectId, final int objectInstanceId, final int resourceId, final LwM2mNode node,
            final ContentFormat contentFormat, final boolean replaceResources) {
        this(new LwM2mPath(objectId, objectInstanceId, resourceId), node, contentFormat, replaceResources);
    }

    public WriteRequest(final String target, final LwM2mNode node, final ContentFormat contentFormat,
            final boolean replaceResources) {
        this(new LwM2mPath(target), node, contentFormat, replaceResources);
    }

    private WriteRequest(final LwM2mPath target, final LwM2mNode node, ContentFormat format,
            final boolean replaceResources) {
        super(target);
        Validate.notNull(node);

        // Validate node and path coherence
        if (getPath().isResource()) {
            if (!(node instanceof LwM2mResource)) {
                throw new IllegalArgumentException(String.format("path '%s' and node type '%s' does not match",
                        target.toString(), node.getClass().getSimpleName()));
            }
        }
        if (getPath().isObjectInstance()) {
            if (!(node instanceof LwM2mObjectInstance)) {
                throw new IllegalArgumentException(String.format("path '%s' and node type '%s' does not match",
                        target.toString(), node.getClass().getSimpleName()));
            }
        }
        if (getPath().isObject()) {
            if (!(node instanceof LwM2mObject)) {
                throw new IllegalArgumentException(String.format("path '%s' and node type '%s' does not match",
                        target.toString(), node.getClass().getSimpleName()));
            }
        }

        // Validate content format
        if (ContentFormat.TEXT == format || ContentFormat.OPAQUE == format) {
            if (!getPath().isResource()) {
                throw new IllegalArgumentException(String.format("%s format must be used only for single resources",
                        format.toString()));
            } else {
                final ResourceSpec description = Resources.getResourceSpec(getPath().getObjectId(), getPath()
                        .getResourceId());
                if (description != null && description.multiple) {
                    throw new IllegalArgumentException(String.format(
                            "%s format must be used only for single resources", format.toString()));
                }
                if (((LwM2mResource) node).isMultiInstances()) {
                    throw new IllegalArgumentException(String.format(
                            "%s format must be used only for single resources", format.toString()));
                }
            }
        }

        // Manage default format
        if (format == null) {
            // Use text for single resource ...
            if (getPath().isResource()) {
                // Use resource description to guess
                final ResourceSpec description = Resources.getResourceSpec(getPath().getObjectId(), getPath()
                        .getResourceId());
                if (description != null) {
                    if (description.multiple) {
                        format = ContentFormat.TLV;
                    } else {
                        format = description.type == Type.OPAQUE ? ContentFormat.OPAQUE : ContentFormat.TEXT;
                    }
                }
                // If no object description available, use 'node' to guess
                else {
                    LwM2mResource resourceNode = ((LwM2mResource) node);
                    if (resourceNode.isMultiInstances()) {
                        format = ContentFormat.TLV;
                    } else {
                        format = resourceNode.getValue().type == DataType.OPAQUE ? ContentFormat.OPAQUE
                                : ContentFormat.TEXT;
                    }
                }
            }
            // ... and TLV for other ones.
            else {
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
    public void accept(final DownlinkRequestVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("WriteRequest [replaceRequest=%s, getPath()=%s]", replaceRequest, getPath());
    }

}