/*
 * Copyright (c) 2013, Sierra Wireless,
 * Copyright (c) 2014, Zebra Technologies,
 * 
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
package leshan.client.lwm2m.bootstrap;

import java.net.InetSocketAddress;
import java.util.Collections;

import leshan.client.lwm2m.Uplink;
import leshan.client.lwm2m.response.Callback;
import leshan.client.lwm2m.response.OperationResponse;

import org.apache.commons.lang.Validate;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.CoAPEndpoint;

public class BootstrapUplink extends Uplink {
    private static final String ENDPOINT = "ep";

    public BootstrapUplink(final InetSocketAddress destination, final CoAPEndpoint origin,
            final BootstrapDownlink downlink) {
        super(destination, origin);
        Validate.notNull(downlink, "BootstrapDownlink must not be null");
    }

    public OperationResponse bootstrap(final String endpointName, final long timeout) {
        final Request request = Request.newPost();
        final BootstrapEndpoint bootstrapEndpoint = new BootstrapEndpoint(Collections.singletonMap(ENDPOINT,
                endpointName));
        request.setURI(bootstrapEndpoint.toString());
        checkStarted(origin);

        return sendSyncRequest(timeout, request);
    }

    public void bootstrap(final String endpointName, final Callback callback) {
        final Request request = Request.newPost();
        final BootstrapEndpoint bootstrapEndpoint = new BootstrapEndpoint(Collections.singletonMap(ENDPOINT,
                endpointName));
        request.setURI(bootstrapEndpoint.toString());

        sendAsyncRequest(callback, request);
    }

}
