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

import leshan.ObserveSpec;
import leshan.core.node.LwM2mPath;
import leshan.core.response.ClientResponse;
import leshan.server.client.Client;
import leshan.util.Validate;

public class WriteAttributesRequest extends AbstractLwM2mRequest<ClientResponse> {

    private final ObserveSpec observeSpec;

    public WriteAttributesRequest(final Client client, final int objectId, final ObserveSpec observeSpec) {
        this(client, new LwM2mPath(objectId), observeSpec);
    }

    public WriteAttributesRequest(final Client client, final int objectId, final int objectInstanceId, final ObserveSpec observeSpec) {
        this(client, new LwM2mPath(objectId, objectInstanceId), observeSpec);
    }

    public WriteAttributesRequest(final Client client, final int objectId, final int objectInstanceId, final int resourceId,
            final ObserveSpec observeSpec) {
        this(client, new LwM2mPath(objectId, objectInstanceId, resourceId), observeSpec);
    }

    public WriteAttributesRequest(final Client client, final String path, final ObserveSpec observeSpec) {
        this(client, new LwM2mPath(path), observeSpec);
    }

    private WriteAttributesRequest(final Client client, final LwM2mPath path, final ObserveSpec observeSpec) {
        super(client, path);
        Validate.notNull(observeSpec);
        this.observeSpec = observeSpec;
    }

    @Override
    public void accept(final LwM2mRequestVisitor visitor) {
        visitor.visit(this);
    }

    public ObserveSpec getObserveSpec() {
        return this.observeSpec;
    }

    @Override
    public String toString() {
        return String.format("WriteAttributesRequest [%s, attributes=%s]", getPath(), getObserveSpec());
    }
}
