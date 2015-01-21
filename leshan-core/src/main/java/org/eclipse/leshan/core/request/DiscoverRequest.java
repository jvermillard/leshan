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
package org.eclipse.leshan.core.request;

import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.response.DiscoverResponse;

public class DiscoverRequest extends AbstractDownlinkRequest<DiscoverResponse> {

    /**
     * Creates a request for discovering the resources implemented by a client for a particular object type.
     *
     * @param objectId the object type
     */
    public DiscoverRequest(int objectId) {
        this(new LwM2mPath(objectId));
    }

    /**
     * Creates a request for discovering the resources implemented by a client for a particular object instance.
     *
     * @param objectId the object type
     * @param objectInstanceId the object instance
     */
    public DiscoverRequest(int objectId, int objectInstanceId) {
        this(new LwM2mPath(objectId, objectInstanceId));
    }

    /**
     * Creates a request for discovering the attributes of a particular resource implemented by a client.
     *
     * @param client the LWM2M Client to discover resources for
     * @param objectId the object type
     * @param objectInstanceId the object instance
     * @param resourceId the resource
     */
    public DiscoverRequest(int objectId, int objectInstanceId, int resourceId) {
        this(new LwM2mPath(objectId, objectInstanceId, resourceId));
    }

    /**
     * Create a request for discovering the attributes of a particular object/instance/resource targeted by a specific
     * path.
     *
     * @param client the LWM2M Client to discover resources for
     * @param target the target path
     */
    public DiscoverRequest(String target) {
        super(new LwM2mPath(target));
    }

    private DiscoverRequest(LwM2mPath target) {
        super(target);
    }

    @Override
    public void accept(DownlinkRequestVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public final String toString() {
        return String.format("DiscoverRequest [%s]", getPath());
    }
}
